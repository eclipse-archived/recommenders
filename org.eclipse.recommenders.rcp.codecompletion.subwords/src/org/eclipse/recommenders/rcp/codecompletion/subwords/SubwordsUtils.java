/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion proposals relevance benchmark
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SubwordsUtils {

    public static final int PREFIX_BONUS = 5000;

    public static int calculateMatchingNGrams(final String s1, final String s2, final int n) {
        return calculateMatchingNGrams(createLowerCaseNGrams(s1, n), createLowerCaseNGrams(s2, n));
    }

    private static int calculateMatchingNGrams(final List<String> ngrams1, final List<String> ngrams2) {
        final Set<String> nGrams1 = Sets.newHashSet(ngrams1);
        final Set<String> nGrams2 = Sets.newHashSet(ngrams2);
        nGrams1.retainAll(nGrams2);
        return nGrams1.size();
    }

    public static List<String> createLowerCaseNGrams(String candidate, final int n) {
        candidate = candidate.toLowerCase();
        final List<String> nGrams = Lists.newLinkedList();
        for (int i = 0; i + n <= candidate.length(); i++) {
            final String nGram = candidate.substring(i, i + n);
            nGrams.add(nGram);
        }
        return nGrams;
    }

    public static StyledString createStyledProposalDisplayString(final StyledString displayString,
            final String completionToken) {
        final StyledString copy = deepCopy(displayString);

        final String string = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(copy.getString());
        for (final String bigram : SubwordsUtils.createLowerCaseNGrams(completionToken, 2)) {
            final int indexOf = StringUtils.indexOfIgnoreCase(string, bigram);
            if (indexOf != -1) {
                copy.setStyle(indexOf, bigram.length(), StyledString.COUNTER_STYLER);
            }
        }
        copy.append(" (partial)", StyledString.COUNTER_STYLER);
        return copy;
    }

    public static StyledString deepCopy(final StyledString displayString) {
        final StyledString copy = new StyledString(displayString.getString());
        for (final StyleRange range : displayString.getStyleRanges()) {
            copy.setStyle(range.start, range.length, new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.background = range.background;
                    textStyle.borderColor = range.borderColor;
                    textStyle.borderStyle = range.borderStyle;
                    textStyle.font = range.font;
                    textStyle.foreground = range.foreground;
                }
            });
        }
        return copy;
    }

    public static boolean checkStringMatchesPrefixPattern(final String prefix, String toTest) {
        final Pattern pattern = createRegexPatternFromPrefix(prefix);
        toTest = getTokensBetweenLastWhitespaceAndFirstOpeningBracket(toTest);
        final Matcher m = pattern.matcher(toTest);
        final boolean matches = m.matches();
        return matches;
    }

    public static Pattern createRegexPatternFromPrefix(final String prefixToken) {

        final StringBuilder sb = new StringBuilder();
        sb.append(".*");
        for (final char c : prefixToken.toCharArray()) {
            if (Character.isUpperCase(c)) {
                // if upper case than match words containing this uppercase
                // letter only - the developer might have a clue what she is
                // looking for...
                sb.append(c);
            } else {
                // if not just search for any proposal containing this letter in
                // upper case OR lower case.
                final char lowerCase = Character.toUpperCase(c);
                sb.append("([").append(lowerCase).append(c).append("])");
            }
            sb.append(".*");
        }
        final String regex = sb.toString();
        final Pattern p = Pattern.compile(regex);
        return p;
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(final char[] completion) {
        return getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String.valueOf(completion));
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String completion) {
        completion = StringUtils.substringBefore(completion, "(");
        if (completion.contains(" ")) {
            completion = StringUtils.substringAfterLast(completion, " ");
        }
        return completion;
    }

    public static int calculateRelevance(final SubwordsProposalContext state) {
        final List<String> prefixBigrams = state.getPrefixBigrams();
        final List<String> matchingRegionBigrams = state.getMatchingRegionBigrams();
        final int matches = calculateMatchingNGrams(prefixBigrams, matchingRegionBigrams);

        int relevance = state.getJdtProposal().getRelevance() + matches;
        if (state.isPrefixMatch()) {
            relevance += PREFIX_BONUS;
        }
        return relevance;
    }
}
