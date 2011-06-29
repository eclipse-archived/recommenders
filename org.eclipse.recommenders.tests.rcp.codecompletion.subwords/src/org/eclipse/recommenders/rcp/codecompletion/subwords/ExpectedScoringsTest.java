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
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsRelevanceCalculator.calculateRelevance;
import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class ExpectedScoringsTest {

    @Test
    public void testPreferPrefix1() {
        forToken("act").assertOrder("action", "createActionListener");
    }

    @Test
    public void testPreferPrefix2() {
        forToken("set").assertOrder("setSelectedIcon", "getSelectedObject");
    }

    @Test
    public void testPreferPrefix3() {
        forToken("set").assertOrder("setSelectedIcon", "getRegisteredKeyStrokes");
    }

    @Test
    @Ignore()
    public void testPreferCamelCase() {
        forToken("NPE").assertOrder("NullPointerException", "nullpointerexception");
    }

    @Test
    public void testSubwordMatching() {
        forToken("word").assertOrder("word", "setWord", "wxoryd", "somethingDifferent");
    }

    private ScoreAssertionHelper forToken(final String token) {
        return ScoreAssertionHelper.create(token);
    }

    private static class ScoreAssertionHelper {

        private String token;

        private static ScoreAssertionHelper create(final String token) {
            final ScoreAssertionHelper helper = new ScoreAssertionHelper();
            helper.token = token;
            return helper;
        }

        public void assertOrder(final String... candidates) {
            float previousScore = Float.MAX_VALUE;
            for (final String candidate : candidates) {
                final float currentScore = calculateRelevance(token, candidate);
                if (currentScore >= previousScore) {
                    Assert.fail("Order by calculated scores differ from expected order for token '" + token + "':"
                            + createDebugString(candidates));
                }
                previousScore = currentScore;
            }
        }

        private String createDebugString(final String[] candidates) {
            final StringBuilder builder = new StringBuilder();
            for (final String candidate : candidates) {
                builder.append(String.format("%n%d %s", calculateRelevance(token, candidate), candidate));
            }
            return builder.toString();
        }
    }
}
