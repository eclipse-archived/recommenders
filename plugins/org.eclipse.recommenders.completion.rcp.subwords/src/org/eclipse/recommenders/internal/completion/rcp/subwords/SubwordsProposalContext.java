/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SubwordsProposalContext {

    public static final int PREFIX_BONUS = 5000;

    private static Styler BIGRAMS_STYLER = new Styler() {
        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.COUNTER_COLOR);
        }
    };

    private static Styler REGEX_STYLER = new Styler() {

        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.underline = true;
        }
    };

    private static Styler COMPOUND_STYLER = new Styler() {

        @Override
        public void applyStyles(final TextStyle textStyle) {
            BIGRAMS_STYLER.applyStyles(textStyle);
            REGEX_STYLER.applyStyles(textStyle);
        }
    };

    private String prefix;
    private final String subwordsMatchingRegion;
    private final IJavaCompletionProposal jdtProposal;
    private final CompletionProposal proposal;
    private final JavaContentAssistInvocationContext ctx;
    private List<String> prefixBigrams;
    private List<String> matchingRegionBigrams;
    private Pattern pattern;

    // private final int maxLevenshteinDistance;

    public SubwordsProposalContext(final String prefix, final CompletionProposal proposal,
            final IJavaCompletionProposal jdtProposal, final JavaContentAssistInvocationContext ctx) {
        this.proposal = ensureIsNotNull(proposal);
        this.ctx = ensureIsNotNull(ctx);
        setPrefix(prefix);
        this.subwordsMatchingRegion = SubwordsUtils.getTokensBetweenLastWhitespaceAndFirstOpeningBracket(jdtProposal
                .getDisplayString());
        this.jdtProposal = ensureIsNotNull(jdtProposal);
        // maxLevenshteinDistance = max(1, (int) floor(log(prefix.length())));
        calculateMatchingRegionBigrams();
    }

    private void calculateMatchingRegionBigrams() {
        matchingRegionBigrams = SubwordsUtils.createLowerCaseNGrams(subwordsMatchingRegion, 2);
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
        this.pattern = SubwordsUtils.createRegexPatternFromPrefix(prefix);
        this.prefixBigrams = SubwordsUtils.createLowerCaseNGrams(prefix, 2);
    }

    public boolean isRegexMatchButNoPrefixMatch() {
        if (subwordsMatchingRegion.startsWith(prefix)) {
            return false;
        }
        return createMatcher().matches();
    }

    public boolean isRegexMatch() {
        return createMatcher().matches();// || passesLevenshteinDistanceFilter();
    }

    // private boolean passesLevenshteinDistanceFilter() {
    // if (prefix.length() < 2) {
    // return false;
    // }
    //
    // prefix = prefix.toLowerCase();
    // final String completionPrefix = substring(subwordsMatchingRegion, 0, prefix.length()).toLowerCase();
    // final int distance = getLevenshteinDistance(completionPrefix, prefix, maxLevenshteinDistance);
    // // no exact matches:
    // if (distance <= 0) {
    // return false;
    // }
    // return true;
    // }

    private Matcher createMatcher() {
        return pattern.matcher(subwordsMatchingRegion);
    }

    public StyledString getStyledDisplayString(final StyledString origin) {
        final StyledString copy = SubwordsUtils.deepCopy(origin);
        final List<SourceRange> bigramHighlightRanges = findBigramHighlightRanges();
        final List<SourceRange> regexHighlightRanges = findRegexHighlightRanges();
        final Set<SourceRange> intersections = findIntersections(bigramHighlightRanges, regexHighlightRanges);

        setStyle(copy, bigramHighlightRanges, BIGRAMS_STYLER);
        setStyle(copy, regexHighlightRanges, REGEX_STYLER);
        setStyle(copy, intersections, COMPOUND_STYLER);

        return copy;
    }

    protected Set<SourceRange> findIntersections(final List<SourceRange> ranges1, final List<SourceRange> ranges2) {
        final Set<SourceRange> intersections = Sets.newHashSet();
        for (final SourceRange range1 : ranges1) {
            for (final SourceRange range2 : ranges2) {
                final int start = Math.max(range1.getOffset(), range2.getOffset());
                final int end = Math.min(range1.getOffset() + range1.getLength(),
                        range2.getOffset() + range2.getLength());
                if (start < end) {
                    intersections.add(new SourceRange(start, end - start));
                }
            }
        }
        return intersections;
    }

    private void setStyle(final StyledString copy, final Collection<SourceRange> ranges, final Styler styler) {
        for (final SourceRange range : ranges) {
            copy.setStyle(range.getOffset(), range.getLength(), styler);
        }
    }

    protected List<SourceRange> findBigramHighlightRanges() {
        final List<SourceRange> res = Lists.newLinkedList();
        for (final String bigram : prefixBigrams) {
            final int indexOf = StringUtils.indexOfIgnoreCase(subwordsMatchingRegion, bigram);
            if (indexOf != -1) {
                final SourceRange range = new SourceRange(indexOf, bigram.length());
                res.add(range);
            }
        }
        return res;
    }

    protected List<SourceRange> findRegexHighlightRanges() {
        final Matcher m = createMatcher();
        final List<SourceRange> res = Lists.newLinkedList();
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                final int start = m.start(i);
                final int end = m.end(i);
                final int length = end - start;
                res.add(new SourceRange(start, length));
            }
        }
        return res;
    }

    public List<String> getPrefixBigrams() {
        return prefixBigrams;
    }

    public List<String> getMatchingRegionBigrams() {
        return matchingRegionBigrams;
    }

    public boolean isPrefixMatch() {
        return subwordsMatchingRegion.startsWith(prefix);
    }

    public int calculateRelevance() {
        if (subwordsMatchingRegion.startsWith(prefix)) {
            return jdtProposal.getRelevance();
        } else {
            return 5;
        }

        // TODO until https://bugs.eclipse.org/bugs/show_bug.cgi?id=350991 is
        // fixed this should not be used:
        // final int matches =
        // SubwordsUtils.calculateMatchingNGrams(prefixBigrams,
        // matchingRegionBigrams);
        //
        // int relevance = jdtProposal.getRelevance() + matches;
        // if (isPrefixMatch()) {
        // relevance += PREFIX_BONUS;
        // }
        // return relevance;
    }

}
