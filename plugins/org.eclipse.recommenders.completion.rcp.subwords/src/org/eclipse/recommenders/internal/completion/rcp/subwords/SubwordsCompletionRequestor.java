/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.matchesPrefixPattern;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.subwords.proposals.ProposalFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SubwordsCompletionRequestor extends CompletionRequestor {

    private final List<IJavaCompletionProposal> proposals = Lists.newLinkedList();

    private final Set<String> duplicates = Sets.newHashSet();
    private final JavaContentAssistInvocationContext ctx;

    private final CompletionProposalCollector collector;

    private final String prefix;

    public SubwordsCompletionRequestor(final String prefix, final JavaContentAssistInvocationContext ctx) {
        super(false);
        checkNotNull(prefix);
        checkNotNull(ctx);
        this.prefix = prefix;
        this.ctx = ctx;

        if (shouldFillArgumentNames()) {
            collector = new FillArgumentNamesCompletionProposalCollector(ctx);
        } else {
            collector = new CompletionProposalCollector(ctx.getCompilationUnit(), false);
        }

        // this.collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        this.collector.acceptContext(ctx.getCoreContext());

        collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, false);
        collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, false);
        collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
        collector.setIgnored(CompletionProposal.FIELD_REF, false);
        collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(CompletionProposal.KEYWORD, false);
        collector.setIgnored(CompletionProposal.LABEL_REF, false);
        collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
        collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
        collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, false);
        collector.setIgnored(CompletionProposal.METHOD_REF, false);
        collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, false);
        collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, false);
        collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
        collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, false);
        collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
        collector.setIgnored(CompletionProposal.TYPE_REF, false);

        collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
        collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
        collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);
        collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
        collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
        collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);
        collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF,
                true);
        collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
                CompletionProposal.TYPE_REF, true);
        collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
                CompletionProposal.TYPE_REF, true);
        collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);

        collector.setFavoriteReferences(getFavoriteStaticMembers());
        collector.setRequireExtendedContext(true);
    }

    @VisibleForTesting
    protected boolean shouldFillArgumentNames() {
        try {
            // when running a test suite this throws a NPE
            return PreferenceConstants.getPreferenceStore().getBoolean(
                    PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES);
        } catch (final Exception e) {
            return true;
        }
    }

    @Override
    public boolean isAllowingRequiredProposals(final int proposalKind, final int requiredProposalKind) {
        return collector.isAllowingRequiredProposals(proposalKind, requiredProposalKind);
    };

    @Override
    public boolean isIgnored(final int completionProposalKind) {
        return collector.isIgnored(completionProposalKind);
    };

    @Override
    public void acceptContext(final CompletionContext context) {
        super.acceptContext(context);
        collector.acceptContext(context);
    }

    @Override
    public boolean isExtendedContextRequired() {
        return collector.isExtendedContextRequired();
    }

    private String[] getFavoriteStaticMembers() {
        final String serializedFavorites = PreferenceConstants.getPreferenceStore().getString(
                PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
        if (serializedFavorites != null && serializedFavorites.length() > 0) {
            return serializedFavorites.split(";"); //$NON-NLS-1$
        }
        return new String[0];
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        if (isDuplicate(proposal)) {
            return;
        }

        final String subwordsMatchingRegion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal);
        if (!matchesPrefixPattern(prefix, subwordsMatchingRegion)) { // && !isSmallTypo(prefix, subwordsMatchingRegion))
                                                                     // {
            return;
        }

        for (final IJavaCompletionProposal p : tryCreateJdtProposal(proposal)) {
            final SubwordsProposalContext subwordsContext = new SubwordsProposalContext(prefix, proposal, p, ctx);
            createSubwordsProposal(subwordsContext);
        }

    }

    private boolean isDuplicate(final CompletionProposal proposal) {
        final StringBuilder sb = new StringBuilder();

        final char[] c = proposal.getCompletion();
        sb.append(c);
        final char[] d = proposal.getDeclarationSignature();
        if (d != null) {
            sb.append(d);
        }
        final char[] s = proposal.getSignature();
        if (s != null) {
            sb.append(s);
        }
        final String key = sb.toString();
        return !duplicates.add(key);
    }

    // private boolean isSmallTypo(final String prefix, final String subwordsMatchingRegion) {
    // if (prefix.length() < 2) {
    // return false;
    // }
    // final int maxDistance = max((int) floor(log(prefix.length())));
    //
    // final String lowerTokenPrefix = prefix.toLowerCase();
    // final String lowerCompletionPrefix = substring(subwordsMatchingRegion, 0, prefix.length()).toLowerCase();
    // final int distance = getLevenshteinDistance(lowerCompletionPrefix, lowerTokenPrefix, maxDistance);
    // // no exact matches:
    // if (distance <= 0) {
    // return false;
    // }
    // return true;
    // }

    private IJavaCompletionProposal[] tryCreateJdtProposal(final CompletionProposal proposal) {
        final int oldLength = collector.getJavaCompletionProposals().length;
        collector.accept(proposal);
        // order matters ;)
        final IJavaCompletionProposal[] jdtProposals = collector.getJavaCompletionProposals();
        final IJavaCompletionProposal[] newProposals = ArrayUtils
                .subarray(jdtProposals, oldLength, jdtProposals.length);
        return newProposals;
    }

    private void createSubwordsProposal(final SubwordsProposalContext subwordsContext) {
        final IJavaCompletionProposal subWordProposal = ProposalFactory.createFromJDTProposal(subwordsContext);
        if (subWordProposal != null) {
            // subWordProposal.setRelevance(subwordsContext.calculateRelevance());
            proposals.add(subWordProposal);
        }
    }

    public List<IJavaCompletionProposal> getProposals() {
        return proposals;
    }

    public void setReplacementLength(final int y) {
        collector.setReplacementLength(y);
    }
}