/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Marcel Bruch - added support for matching and scoring subsequences.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;

public class SubwordsProposalContext {

    public static final int PREFIX_BONUS = 5000;

    private static Styler BIGRAMS_STYLER = new Styler() {
        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.COUNTER_COLOR);
        }
    };

    private String prefix;
    private final String subwordsMatchingRegion;
    private final IJavaCompletionProposal jdtProposal;
    private final CompletionProposal proposal;
    private final JavaContentAssistInvocationContext ctx;

    private int[] bestSubsequence;
    private int bestSubsequenceScore;

    public SubwordsProposalContext(final String prefix, final CompletionProposal proposal,
            final IJavaCompletionProposal jdtProposal, final JavaContentAssistInvocationContext ctx) {
        this.proposal = ensureIsNotNull(proposal);
        this.ctx = ensureIsNotNull(ctx);
        this.subwordsMatchingRegion = SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket(jdtProposal
                .getDisplayString());
        this.jdtProposal = ensureIsNotNull(jdtProposal);
        setPrefix(prefix);
    }

    @SuppressWarnings("unchecked")
    public <T extends IJavaCompletionProposal> T getJdtProposal() {
        return (T) jdtProposal;
    }

    public CompletionProposal getProposal() {
        return proposal;
    }

    public JavaContentAssistInvocationContext getContext() {
        return ctx;
    }

    public void setPrefix(final String prefix) {
        this.prefix = ensureIsNotNull(prefix);
        bestSubsequence = LCSS.bestSubsequence(subwordsMatchingRegion, prefix);
        bestSubsequenceScore = LCSS.scoreSubsequence(bestSubsequence);
    }

    public StyledString getStyledDisplayString(final StyledString origin) {
        final StyledString copy = SubwordsUtils.deepCopy(origin);
        for (int index : bestSubsequence)
            copy.setStyle(index, 1, BIGRAMS_STYLER);
        return copy;
    }

    public boolean isPrefixMatch() {
        return subwordsMatchingRegion.startsWith(prefix);
    }

    public int calculateRelevance() {
        String commonPrefix = getCommonPrefix(subwordsMatchingRegion, prefix);
        int relevance = jdtProposal.getRelevance() + commonPrefix.length() + bestSubsequenceScore;
        return relevance;
    }

    public boolean isRegexMatch() {
        return LCSS.containsSubsequence(subwordsMatchingRegion, prefix);
    }
}
