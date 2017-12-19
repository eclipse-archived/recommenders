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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.JAVA_PROPOSALS;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.subwords.rcp.LCSS.containsSubsequence;
import static org.eclipse.recommenders.internal.subwords.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.CompletionContexts;
import org.eclipse.recommenders.completion.rcp.HtmlTagProposals;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.NoProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.Proposals;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.internal.subwords.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.recommenders.utils.rcp.TimeDelimitedProgressMonitor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;

@SuppressWarnings("restriction")
public class SubwordsSessionProcessor extends SessionProcessor {

    /**
     * Use the timeout set by {@link JavaCompletionProposalComputer#JAVA_CODE_ASSIST_TIMEOUT}.
     */
    private static final long COMPLETION_TIME_OUT = Long.getLong("org.eclipse.jdt.ui.codeAssistTimeout", 5000); //$NON-NLS-1$

    private static final int JAVADOC_TYPE_REF_HIGHLIGHT_ADJUSTMENT = "{@link ".length(); //$NON-NLS-1$

    // Negative value ensures subsequence matches have a lower relevance than standard JDT or template proposals
    public static final int SUBWORDS_RANGE_START = -9000;
    public static final int CASE_SENSITIVE_EXACT_MATCH_START = 16
            * (RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_CASE);
    public static final int CASE_INSENSITIVE_EXACT_MATCH_START = 16 * RelevanceConstants.R_EXACT_NAME;

    private static final int[] EMPTY_SEQUENCE = new int[0];

    private static final Class<?> BOLD_STYLER_PROVIDER = Reflections
            .loadClass(SubwordsSessionProcessor.class.getClassLoader(),
                    "org.eclipse.jface.text.contentassist.BoldStylerProvider") //$NON-NLS-1$
            .orNull();
    private static final Constructor<?> NEW_BOLD_STYLER_PROVIDER = Reflections
            .getDeclaredConstructor(BOLD_STYLER_PROVIDER, Font.class).orNull();
    private static final Method GET_BOLD_STYLER = Reflections.getDeclaredMethod(BOLD_STYLER_PROVIDER, "getBoldStyler") //$NON-NLS-1$
            .orNull();
    private static final Method DISPOSE = Reflections.getDeclaredMethod(BOLD_STYLER_PROVIDER, "dispose") //$NON-NLS-1$
            .orNull();

    private Object stylerProvider;
    private Styler styler;

    private static final Field CORE_CONTEXT = Reflections
            .getDeclaredField(true, JavaContentAssistInvocationContext.class, "fCoreContext").orNull(); //$NON-NLS-1$
    private static final Field CU = Reflections.getDeclaredField(true, JavaContentAssistInvocationContext.class, "fCU") //$NON-NLS-1$
            .orNull();
    private static final Field CU_COMPUTED = Reflections
            .getDeclaredField(true, JavaContentAssistInvocationContext.class, "fCUComputed").orNull(); //$NON-NLS-1$

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

            Map<IJavaCompletionProposal, CompletionProposal> baseProposals = new HashMap<>();

            recContext.set(JAVA_PROPOSALS, baseProposals);

            ASTNode completionNode = compContext.getCompletionNode();
            ASTNode completionNodeParent = compContext.getCompletionNodeParent();
            SortedSet<Integer> triggerlocations = computeTriggerLocations(offset, completionNode, completionNodeParent,
                    length);

            Set<String> sortkeys = new HashSet<>();
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
        SortedSet<Integer> triggerlocations = new TreeSet<>(Ordering.natural().reverse());
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
            return new HashMap<>();
        }
        ICompilationUnit cu = originalContext.getCompilationUnit();
        ITextViewer viewer = originalContext.getViewer();
        IEditorPart editor = lookupEditor(cu);
        JavaContentAssistInvocationContext newJdtContext = new JavaContentAssistInvocationContext(viewer, triggerOffset,
                editor);
        setCompilationUnit(newJdtContext, cu);
        ProposalCollectingCompletionRequestor collector = computeProposals(cu, newJdtContext, triggerOffset);
        Map<IJavaCompletionProposal, CompletionProposal> proposals = collector.getProposals();
        return proposals != null ? proposals : new HashMap<IJavaCompletionProposal, CompletionProposal>();
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
            case CompletionProposal.JAVADOC_INLINE_TAG:
            case Proposals.MODULE_DECLARATION:
            case Proposals.MODULE_REF: {
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

                if (bestSequence.length > 0) {
                    // We will highlight on demand in modifyDisplayString.
                    proposal.setTag(IS_HIGHLIGHTED, true);
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
                    displayString.setStyle(index + highlightAdjustment, 1, getStyler());
                }
            }

            /**
             * The fundamental logic of this method has been taken from the method
             * {@link org.eclipse.jdt.internal.codeassist.CompletionEngine#computeRelevanceForCaseMatching}
             */
            @Override
            public int modifyRelevance() {
                if (ArrayUtils.isEmpty(bestSequence)) {
                    proposal.setTag(IS_PREFIX_MATCH, true);
                    return 0;
                }

                int relevanceBoost = 0;

                /*
                 * In providing subwords proposals we simulate two content assist triggers. The first is at position 0
                 * and the second at the position of the minimum prefix length. For the case that the prefix is longer
                 * than the minimum prefix length we need to take extra steps to ensure that possible recommendations
                 * which would be an exact match to this full prefix are given a boost, as they would be for
                 * non-subwords proposals. (see Bug 468494)
                 *
                 * The boost for full prefix matches is the same one as JDT adds at {@link
                 * org.eclipse.jdt.internal.codeassist.CompletionEngine#computeRelevanceForCaseMatching}
                 *
                 * The boost is further multiplied by 16 which reflects what happens in {@link
                 * org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeRelevance}
                 */
                if (StringUtils.equals(prefix, matchingArea)) {
                    if (minPrefixLengthForTypes < prefix.length()) {
                        relevanceBoost = CASE_SENSITIVE_EXACT_MATCH_START;
                    }
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_EXACT_MATCH, true);
                    proposal.setTag(IS_PREFIX_MATCH, true);
                } else if (StringUtils.equalsIgnoreCase(prefix, matchingArea)) {
                    if (minPrefixLengthForTypes < prefix.length()) {
                        relevanceBoost = CASE_INSENSITIVE_EXACT_MATCH_START;
                    }
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_EXACT_MATCH, true);
                    proposal.setTag(IS_CASE_INSENSITIVE_PREFIX_MATCH, true);
                } else if (StringUtils.startsWithIgnoreCase(matchingArea, prefix)) {
                    // Don't adjust score
                    proposal.setTag(SUBWORDS_SCORE, null);
                    proposal.setTag(IS_CASE_INSENSITIVE_PREFIX_MATCH, true);
                } else if (CharOperation.camelCaseMatch(prefix.toCharArray(), matchingArea.toCharArray())) {
                    // Don't adjust score
                    proposal.setTag(IS_CAMEL_CASE_MATCH, true);
                } else {
                    int score = LCSS.scoreSubsequence(bestSequence);
                    proposal.setTag(SUBWORDS_SCORE, score);
                    relevanceBoost = SUBWORDS_RANGE_START + score;
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

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        styler = null;

        if (DISPOSE != null && stylerProvider != null) {
            try {
                DISPOSE.invoke(stylerProvider);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            }
        }

        stylerProvider = null;
    }

    private Styler getStyler() {
        if (styler != null) {
            return styler;
        }

        if (NEW_BOLD_STYLER_PROVIDER != null && GET_BOLD_STYLER != null) {
            try {
                stylerProvider = NEW_BOLD_STYLER_PROVIDER.newInstance(JFaceResources.getDefaultFont());
                styler = (Styler) GET_BOLD_STYLER.invoke(stylerProvider);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | InstantiationException | SecurityException e) {
                styler = StyledString.COUNTER_STYLER;
            }
        } else {
            styler = StyledString.COUNTER_STYLER;
        }

        return styler;
    }
}
