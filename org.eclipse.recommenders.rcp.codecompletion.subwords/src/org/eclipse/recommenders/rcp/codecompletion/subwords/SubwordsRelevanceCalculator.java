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

import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.createRegexPatternFromPrefix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.recommenders.commons.utils.Checks;

public class SubwordsRelevanceCalculator {

    public static final int PREFIX_BONUS = 5000;

    private final Pattern pattern;
    private final String token;
    private int jdtRelevance;
    private int nGramMatches;
    private String completion;

    public SubwordsRelevanceCalculator(final String token) {
        this.token = Checks.ensureIsNotNull(token);
        this.pattern = createRegexPatternFromPrefix(token);
    }

    public void setCompletion(final String completion) {
        this.completion = completion;
        calculateNGramMatches(token, completion);
    }

    public void setJdtRelevance(final int jdtRelevance) {
        this.jdtRelevance = jdtRelevance;
    }

    public int getRelevance() {
        int relevance = jdtRelevance + nGramMatches;
        if (isTokenPrefix()) {
            relevance += PREFIX_BONUS;
        }
        return relevance;
    }

    public boolean isRelevant() {
        final Matcher m = pattern.matcher(completion);
        return m.matches();
    }

    private void calculateNGramMatches(String s1, String s2) {
        s1 = prepareString(s1);
        s2 = prepareString(s2);

        nGramMatches = QGramSimilarity.calculateMatchingNGrams(s1, s2, 2);
    }

    private String prepareString(final String s1) {
        if (s1 == null) {
            return "";
        } else {
            return s1.trim().toLowerCase();
        }
    }

    private boolean isTokenPrefix() {
        return completion.startsWith(token);
    }

}
