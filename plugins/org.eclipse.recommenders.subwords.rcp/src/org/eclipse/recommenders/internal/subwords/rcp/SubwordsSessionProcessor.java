/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.*;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.JAVA_PROPOSALS;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.subwords.rcp.LCSS.containsSubsequence;
import static org.eclipse.recommenders.internal.subwords.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnFieldType;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.HTMLTagCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.CompletionContexts;
import org.eclipse.recommenders.completion.rcp.HtmlTagProposals;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.NoProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.internal.subwords.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.recommenders.utils.rcp.TimeDelimitedProgressMonitor;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsSessionProcessor extends SessionProcessor {

    private static final long COMPLETION_TIME_OUT = SECONDS.toMillis(5);

    private static final int JAVADOC_TYPE_REF_HIGHLIGHT_ADJUSTMENT = "{@link ".length(); //$NON-NLS-1$

    // Negative value ensures subsequence matches have a lower relevance than standard JDT or template proposals
    private static final int SUBWORDS_RANGE_START = -9000;
    private static final int CAMEL_CASE_RANGE_START = -6000;
    private static final int IGNORE_CASE_RANGE_START = -3000;

    private static final int[] EMPTY_SEQUENCE = new int[0];

    private static final Field CORE_CONTEXT = Reflections
            .getDeclaredField(JavaContentAssistInvocationContext.class, "fCoreContext").orNull(); //$NON-NLS-1$
    private static final Field CU = Reflections.getDeclaredField(JavaContentAssistInvocationContext.class, "fCU") //$NON-NLS-1$
            .orNull();
    private static final Field CU_COMPUTED = Reflections
            .getDeclaredField(JavaContentAssistInvocationContext.class, "fCUComputed").orNull(); //$NON-NLS-1$

    private final HTMLTagCompletionProposalComputer htmlTagProposalComputer = new HTMLTagCompletionProposalComputer();
    private final SubwordsRcpPreferences prefs;
    private int minPrefixLengthForTypes;

    @Inject
    public SubwordsSessionProcessor(SubwordsRcpPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void initializeContext(IRecommendersCompletionContext recContext) {
        try {
            minPrefixLengthForTypes = prefs.minPrefixLengthForTypes;
            JavaContentAssistInvocationContext jdtContext = recContext.getJavaContext();
            ICompilationUnit cu = jdtContext.getCompilationUnit();
            int offset = jdtContext.getInvocationOffset();

            // Tricky: we bypass the normal code completion request (triggered at the actual cursor position) by
            // replacing all required keys manually and triggering content assist where we need it:

            // TODO maybe we can get rid of that call by simply using the 'right' collector for the first time? This
            // would save ~5 ms I guess.
            NoProposalCollectingCompletionRequestor collector = new NoProposalCollectingCompletionRequestor();
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(COMPLETION_TIME_OUT, MILLISECONDS));
            InternalCompletionContext compContext = collector.getCoreContext();
            if (compContext == null) {
                Logs.log(LogMessages.ERROR_COMPLETION_CONTEXT_NOT_COLLECTED, cu.getPath());
                return;
            }

            CORE_CONTEXT.set(jdtContext, compContext);
            recContext.set(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, compContext);

            String prefix = getPrefix(jdtContext);
            int length = prefix.length();
            recContext.set(CompletionContextKey.COMPLETION_PREFIX, prefix);

            Map<IJavaCompletionProposal, CompletionProposal> baseProposals = Maps.newHashMap();

            recContext.set(JAVA_PROPOSALS, baseProposals);

            ASTNode completionNode = compContext.getCompletionNode();
            ASTNode completionNodeParent = compContext.getCompletionNodeParent();
            SortedSet<Integer> triggerlocations = computeTriggerLocations(offset, completionNode, completionNodeParent,
                    length);

            Set<String> sortkeys = Sets.newHashSet();
            for (int trigger : triggerlocations) {
                Map<IJavaCompletionProposal, CompletionProposal> newProposals = getNewProposals(jdtContext, trigger);
                testAndInsertNewProposals(recContext, baseProposals, sortkeys, newProposals);
            }

            if (jdtContext instanceof JavadocContentAssistInvocationContext) {
                ITextViewer viewer = jdtContext.getViewer();
                IEditorPart editor = lookupEditor(cu);
                JavadocContentAssistInvocationContext newJdtContext = new JavadocContentAssistInvocationContext(viewer,
                        offset - length, editor, 0);
                testAndInsertNewProposals(recContext, baseProposals, sortkeys,
                        HtmlTagProposals.computeHtmlTagProposals(htmlTagProposalComputer, newJdtContext));
            }
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_EXCEPTION_DURING_CODE_COMPLETION, e);
        }
    }

    private SortedSet<Integer> computeTriggerLocations(int offset, ASTNode completionNode, ASTNode completionNodeParent,
            int length) {
        // It is important to trigger at higher locations first, as the base relevance assigned to a proposal by the JDT
        // may depend on the prefix. Proposals which are made for both an empty prefix and a non-empty prefix are thus
        // assigned a base relevance that is as close as possible to that the JDT would assign without subwords
        // completion enabled.
        // TODO MB Need an example.
        SortedSet<Integer> triggerlocations = Sets.newTreeSet(Ordering.natural().reverse());
        int emptyPrefix = offset - length;

        // to make sure we get method stub creation proposals like exe --> private void exe()
        // See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=477801
        if (completionNode instanceof CompletionOnFieldType) {
            triggerlocations.add(offset);
        }
        // Trigger first with either the specified prefix or the specified minimum prefix length. Note that this is only
        // effective for type and constructor completions, but this situation cannot be detected reliably.
        int triggerOffset = min(minPrefixLengthForTypes, length);
        triggerlocations.add(emptyPrefix + triggerOffset);

        // Always trigger with empty prefix to get all members at the current location:
        triggerlocations.add(emptyPrefix);

        return triggerlocations;
    }

    private String getPrefix(JavaContentAssistInvocationContext jdtContext) throws BadLocationException {
        CharSequence prefix = jdtContext.computeIdentifierPrefix();
        return prefix == null ? "" : prefix.toString(); //$NON-NLS-1$
    }

    private Map<IJavaCompletionProposal, CompletionProposal> getNewProposals(
            JavaContentAssistInvocationContext originalContext, int triggerOffset) {
        if (triggerOffset < 0) {
            // XXX not sure when this happens but is has happened in the past
            return Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
        }
        ICompilationUnit cu = originalContext.getCompilationUnit();
        ITextViewer viewer = originalContext.getViewer();
        IEditorPart editor = lookupEditor(cu);
        JavaContentAssistInvocationContext newJdtContext = new JavaContentAssistInvocationContext(viewer, triggerOffset,
                editor);
        setCompilationUnit(newJdtContext, cu);
        ProposalCollectingCompletionRequestor collector = computeProposals(cu, newJdtContext, triggerOffset);
        Map<IJavaCompletionProposal, CompletionProposal> proposals = collector.getProposals();
        return proposals != null ? proposals : Maps.<IJavaCompletionProposal, CompletionProposal>newHashMap();
    }

    private void setCompilationUnit(JavaContentAssistInvocationContext newJdtContext, ICompilationUnit cu) {
        if (Checks.anyIsNull(CU, CU_COMPUTED)) {
            return;
        }
        try {
            CU.set(newJdtContext, cu);
            CU_COMPUTED.set(newJdtContext, true);
        } catch (Exception e) {
            Logs.log(ERROR_EXCEPTION_DURING_CODE_COMPLETION, e);
        }
    }

    private void testAndInsertNewProposals(IRecommendersCompletionContext crContext,
            Map<IJavaCompletionProposal, CompletionProposal> baseProposals, Set<String> sortkeys,
            final Map<IJavaCompletionProposal, CompletionProposal> newProposals) {
        for (Entry<IJavaCompletionProposal, CompletionProposal> entry : newProposals.entrySet()) {
            IJavaCompletionProposal javaProposal = entry.getKey();
            CompletionProposal coreProposal = entry.getValue();

            // we need a completion string (close to a display string) that allows to spot duplicated proposals
            // key point: we don't want to use the display string of lazy completion proposals for performance reasons.
            String completionIdentifier = computeCompletionIdentifier(javaProposal, coreProposal);
            String completion = CompletionContexts.getPrefixMatchingArea(completionIdentifier);

            if (!sortkeys.contains(completionIdentifier) && containsSubsequence(completion, crContext.getPrefix())) {
                baseProposals.put(javaProposal, coreProposal);
                sortkeys.add(completionIdentifier);
            }
        }
    }

    private String computeCompletionIdentifier(IJavaCompletionProposal javaProposal, CompletionProposal coreProposal) {
        String completionIdentifier;
        if (javaProposal instanceof LazyJavaCompletionProposal && coreProposal != null) {
            switch (coreProposal.getKind()) {
            case CompletionProposal.CONSTRUCTOR_INVOCATION: {
                // result: ClassSimpleName(Lsome/Param;I)V
                completionIdentifier = new StringBuilder().append(coreProposal.getName()).append(' ')
                        .append(coreProposal.getSignature()).append(coreProposal.getDeclarationSignature()).toString();
                break;
            }
            case CompletionProposal.JAVADOC_TYPE_REF: {
                // result: ClassSimpleName fully.qualified.ClassSimpleName javadoc
                char[] signature = coreProposal.getSignature();
                char[] simpleName = Signature.getSignatureSimpleName(signature);
                int indexOf = CharOperation.lastIndexOf('.', simpleName);
                simpleName = CharOperation.subarray(simpleName, indexOf + 1, simpleName.length);
                completionIdentifier = new StringBuilder().append(simpleName).append(' ').append(signature)
                        .append(" javadoc").toString(); //$NON-NLS-1$
                break;
            }
            case CompletionProposal.TYPE_REF: {
                // result: ClassSimpleName fully.qualified.ClassSimpleName
                char[] signature = coreProposal.getSignature();
                char[] simpleName = Signature.getSignatureSimpleName(signature);
                int indexOf = CharOperation.lastIndexOf('.', simpleName);
                simpleName = CharOperation.subarray(simpleName, indexOf + 1, simpleName.length);
                completionIdentifier = new StringBuilder().append(simpleName).append(' ').append(signature).toString();
                break;
            }
            case CompletionProposal.PACKAGE_REF:
                // result: org.eclipse.my.package
                completionIdentifier = new String(coreProposal.getDeclarationSignature());
                break;
            case CompletionProposal.METHOD_REF:
            case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            case CompletionProposal.METHOD_NAME_REFERENCE: {
                // result: myMethodName(Lsome/Param;I)V
                completionIdentifier = new StringBuilder().append(coreProposal.getName()).append(' ')
                        .append(coreProposal.getSignature()).append(coreProposal.getDeclarationSignature()).toString();
                break;
            }
            case CompletionProposal.JAVADOC_METHOD_REF: {
                // result: myMethodName(Lsome/Param;I)V
                completionIdentifier = new StringBuilder().append(coreProposal.getName()).append(' ')
                        .append(coreProposal.getSignature()).append(coreProposal.getDeclarationSignature())
                        .append(" javadoc").toString(); //$NON-NLS-1$
                break;
            }
            case CompletionProposal.JAVADOC_PARAM_REF:
            case CompletionProposal.JAVADOC_BLOCK_TAG:
            case CompletionProposal.JAVADOC_INLINE_TAG: {
                completionIdentifier = javaProposal.getDisplayString();
                break;
            }
            default:
                // result: display string. This should not happen. We should issue a warning here...
                completionIdentifier = javaProposal.getDisplayString();
                Logs.log(ERROR_UNEXPECTED_FALL_THROUGH, coreProposal.getKind(), javaProposal.getClass());
                break;
            }
        } else {
            completionIdentifier = javaProposal.getDisplayString();
        }
        return completionIdentifier;
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext crContext) {
        return true;
    }

    private ProposalCollectingCompletionRequestor computeProposals(ICompilationUnit cu,
            JavaContentAssistInvocationContext coreContext, int offset) {
        ProposalCollectingCompletionRequestor collector = new ProposalCollectingCompletionRequestor(coreContext);
        try {
            cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(COMPLETION_TIME_OUT, MILLISECONDS));
        } catch (final Exception e) {
            log(ERROR_EXCEPTION_DURING_CODE_COMPLETION, e);
        }
        return collector;
    }

    @VisibleForTesting
    protected IEditorPart lookupEditor(ICompilationUnit cu) {
        return EditorUtility.isOpenInEditor(cu);
    }

    @Override
    public void process(final IProcessableProposal proposal) {

        String completionIdentifier = computeCompletionIdentifier(proposal, proposal.getCoreProposal().orNull());
        final String matchingArea = CompletionContexts.getPrefixMatchingArea(completionIdentifier);

        proposal.getProposalProcessorManager().addProcessor(new ProposalProcessor() {

            int[] bestSequence = EMPTY_SEQUENCE;

            String prefix;

            @Override
            public boolean isPrefix(String prefix) {
                if (this.prefix != prefix) {
                    this.prefix = prefix;
                    CompletionProposal coreProposal = proposal.getCoreProposal().orNull();
                    if (coreProposal != null
                            && (coreProposal.getKind() == CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER
                                    || coreProposal.getKind() == CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER)) {
                        // This covers the case where the user starts with a prefix of "receiver.ge" and continues
                        // typing 't' from there: In this case, prefix == "receiver.get" rather than "get".
                        // I have only ever encountered this with proposal kinds of *_REF_WITH_CASTED_RECEIVER.
                        int lastIndexOfDot = prefix.lastIndexOf('.');
                        bestSequence = LCSS.bestSubsequence(matchingArea, prefix.substring(lastIndexOfDot + 1));
                    } else {
                        int lastIndexOfHash = prefix.lastIndexOf('#');
                        if (lastIndexOfHash >= 0) {
                            // This covers the case where the user starts with a prefix of "Collections#" and continues
                            // from there.
                            bestSequence = LCSS.bestSubsequence(matchingArea, prefix.substring(lastIndexOfHash + 1));
                        } else {
                            // Besides the obvious, this also covers the case where the user starts with a prefix of
                            // "Collections#e", which manifests itself as just "e".
                            bestSequence = LCSS.bestSubsequence(matchingArea, prefix);
                        }
                    }
                }
                return prefix.isEmpty() || bestSequence.length > 0;
            }

            @Override
            public void modifyDisplayString(StyledString displayString) {
                final int highlightAdjustment;
                CompletionProposal coreProposal = proposal.getCoreProposal().orNull();
                if (coreProposal == null) {
                    // HTML tag proposals are non-lazy(!) JavaCompletionProposals that don't have a core proposal.
                    if (proposal instanceof JavaCompletionProposal && displayString.toString().startsWith("</")) { //$NON-NLS-1$
                        highlightAdjustment = 2;
                    } else if (proposal instanceof JavaCompletionProposal && displayString.toString().startsWith("<")) { //$NON-NLS-1$
                        highlightAdjustment = 1;
                    } else {
                        highlightAdjustment = 0;
                    }
                } else {
                    switch (coreProposal.getKind()) {
                    case CompletionProposal.JAVADOC_FIELD_REF:
                    case CompletionProposal.JAVADOC_METHOD_REF:
                    case CompletionProposal.JAVADOC_VALUE_REF:
                        highlightAdjustment = displayString.toString().lastIndexOf('#') + 1;
                        break;
                    case CompletionProposal.JAVADOC_TYPE_REF:
                        highlightAdjustment = JAVADOC_TYPE_REF_HIGHLIGHT_ADJUSTMENT;
                        break;
                    default:
                        highlightAdjustment = 0;
                    }
                }

                for (int index : bestSequence) {
                    displayString.setStyle(index + highlightAdjustment, 1, StyledString.COUNTER_STYLER);
                }
            }

            /**
             * Since we may simulate completion triggers at positions before the actual triggering, we don't get JDT's
             * additional relevance for exact prefix matches. So we add the additional relevance ourselves, if is not
             * already supplied by the JDT which it does, if the prefix is shorter than the configured minimum prefix
             * length.
             *
             * The boost is the same one as JDT adds at
             * {@link org.eclipse.jdt.internal.codeassist.CompletionEngine#computeRelevanceForCaseMatching}
             *
             * The boost is further multiplied by 16 which reflects the same thing happening in
             * {@link org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeRelevance}
             */
            @Override
            public int modifyRelevance() {
                if (ArrayUtils.isEmpty(bestSequence)) {
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    return 0;
                }

                int relevanceBoost = 0;

                if (minPrefixLengthForTypes < prefix.length() && StringUtils.equalsIgnoreCase(matchingArea, prefix)) {
                    relevanceBoost += 16 * RelevanceConstants.R_EXACT_NAME;
                    proposal.setTag(IS_EXACT_MATCH, true);
                }

                // We only apply case matching to genuine Java proposals, i.e., proposals link HTML tags are ranked
                // together with the case in-sensitive matches.
                if (StringUtils.startsWith(matchingArea, prefix) && isFromJavaCompletionProposalComputer(proposal)) {
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    // Don't adjust relevance.
                } else if (startsWithIgnoreCase(matchingArea, prefix)) {
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_CASE_INSENSITIVE_PREFIX_MATCH, true);
                    relevanceBoost = IGNORE_CASE_RANGE_START + relevanceBoost;
                } else if (CharOperation.camelCaseMatch(prefix.toCharArray(), matchingArea.toCharArray())
                        && isFromJavaCompletionProposalComputer(proposal)) {
                    proposal.setTag(IS_PREFIX_MATCH, false);
                    proposal.setTag(IS_CAMEL_CASE_MATCH, true);
                    relevanceBoost = CAMEL_CASE_RANGE_START + relevanceBoost;
                } else {
                    int score = LCSS.scoreSubsequence(bestSequence);
                    proposal.setTag(IS_PREFIX_MATCH, false);
                    proposal.setTag(SUBWORDS_SCORE, score);
                    relevanceBoost = SUBWORDS_RANGE_START + relevanceBoost + score;
                }

                return relevanceBoost;
            }

            /**
             * Some {@link IProcessableProposal}s are not produced by the {@link JavaCompletionProposalComputer}, but by
             * some other {@link IJavaCompletionProposalComputer}, e.g., the {@link HTMLTagCompletionProposalComputer}.
             * These proposals do not have a core proposal.
             */
            private boolean isFromJavaCompletionProposalComputer(final IProcessableProposal proposal) {
                return proposal.getCoreProposal().isPresent();
            }
        });
    }
}
