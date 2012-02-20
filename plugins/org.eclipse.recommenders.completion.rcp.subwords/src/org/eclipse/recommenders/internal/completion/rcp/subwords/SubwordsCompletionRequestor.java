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
import static java.lang.Math.floor;
import static java.lang.Math.log;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.matchesPrefixPattern;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

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
        this.collector = new CompletionProposalCollector(ctx.getCompilationUnit(), false);
        setFavoriteReferences(getFavoriteStaticMembers());
        this.collector.acceptContext(ctx.getCoreContext());
        setRequireExtendedContext(true);
        this.collector.setFavoriteReferences(getFavoriteStaticMembers());

        // setIgnored(CompletionProposal.TYPE_REF, false);
        // setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, false);
        // setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, false);
        // setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
        // setIgnored(CompletionProposal.FIELD_REF, false);
        // setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, false);
        // setIgnored(CompletionProposal.KEYWORD, false);
        // setIgnored(CompletionProposal.LABEL_REF, false);
        // setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
        // setIgnored(CompletionProposal.METHOD_DECLARATION, false);
        // setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, false);
        // setIgnored(CompletionProposal.METHOD_REF, false);
        // setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, false);
        // setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, false);
        // setIgnored(CompletionProposal.PACKAGE_REF, false);
        // setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, false);
        // setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
        // setIgnored(CompletionProposal.TYPE_REF, false);
        // // Allow completions for unresolved types - since 3.3
        // setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
        // setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
        // setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);
        //
        // setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
        // setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
        // setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);
        //
        // setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
        //
        // setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
        // CompletionProposal.TYPE_REF, true);
        // setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, CompletionProposal.TYPE_REF,
        // true);
        //
        // setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
        //
        // Set the favorite list to propose static members - since 3.3

    }

    @Override
    public boolean isIgnored(final int completionProposalKind) {
        return false;
    }

    @Override
    public boolean isAllowingRequiredProposals(final int proposalKind, final int requiredProposalKind) {
        return true;
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

        final String subwordsMatchingRegion = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(proposal
                .getCompletion());
        if (!matchesPrefixPattern(prefix, subwordsMatchingRegion) && !isSmallTypo(prefix, subwordsMatchingRegion)) {
            return;
        }

        for (final IJavaCompletionProposal p : tryCreateJdtProposal(proposal)) {
            final SubwordsProposalContext subwordsContext = new SubwordsProposalContext(prefix, proposal, p, ctx);
            createSubwordsProposal(subwordsContext);
        }

    }

    private boolean isDuplicate(final CompletionProposal proposal) {
        final String completion = String.valueOf(proposal.getCompletion());
        return !duplicates.add(completion);
    }

    private boolean isSmallTypo(final String prefix, final String subwordsMatchingRegion) {
        if (prefix.length() < 2) {
            return false;
        }
        final int maxDistance = (int) floor(log(prefix.length()));

        final String lowerTokenPrefix = prefix.toLowerCase();
        final String lowerCompletionPrefix = substring(subwordsMatchingRegion, 0, prefix.length()).toLowerCase();
        final int distance = getLevenshteinDistance(lowerCompletionPrefix, lowerTokenPrefix, maxDistance);
        // no exact matches:
        if (distance <= 0) {
            return false;
        }
        return true;
    }

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
        final AbstractJavaCompletionProposal subWordProposal = SubwordsCompletionProposalFactory
                .createFromJDTProposal(subwordsContext);
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