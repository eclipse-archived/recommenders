/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

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
import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockJdtCompletion;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.recommenders.utils.ConcurrentBag;
import org.junit.Test;

public class SubwordsCompletionHighlightingTest {

    @Test
    public void testHighlights01() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("kk", "kkokk");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        final Set<SourceRange> intersections = sut.findIntersections(bigramMatches, regexMatches);
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 0);
        checkCorrectStartingIndexes(regexMatches, 0, 1);
        checkCorrectStartingIndexes(intersections, 0, 1);
        checkCorrectLength(bigramMatches, 2);
        checkCorrectLength(regexMatches, 1);
        checkCorrectLength(intersections, 1);
    }

    @Test
    public void testHighlights03() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("lotat", "setLayoutData");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        final Set<SourceRange> intersections = sut.findIntersections(bigramMatches, regexMatches);
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 11, 10);
        checkCorrectStartingIndexes(regexMatches, 3, 6, 8, 10, 11);
        checkCorrectStartingIndexes(intersections, 10, 11);
        checkCorrectLength(bigramMatches, 2);
        checkCorrectLength(regexMatches, 1);
        checkCorrectLength(intersections, 1);
    }

    @Test
    public void testHighlights02() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("tken", "token()");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        final Set<SourceRange> intersections = sut.findIntersections(bigramMatches, regexMatches);
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 2, 3);
        checkCorrectStartingIndexes(regexMatches, 0, 2, 3, 4);
        checkCorrectStartingIndexes(intersections, 2, 3, 4);
        checkCorrectLength(bigramMatches, 2);
        checkCorrectLength(regexMatches, 1);
        checkCorrectLength(intersections, 1);
    }

    private SubwordsProposalContext createTestContext(final String prefix, final String completion)
            throws JavaModelException {
        final SubwordsProposalContext sut = new SubwordsProposalContext(prefix, mockCompletionProposal(),
                mockJdtCompletion(completion), mockInvocationContext());
        return sut;
    }

    private void checkCorrectStartingIndexes(final Set<SourceRange> ranges, final int... startIndexes) {
        final ArrayList<SourceRange> list = new ArrayList<SourceRange>(ranges);
        Collections.sort(list, new Comparator<SourceRange>() {
            @Override
            public int compare(final SourceRange o1, final SourceRange o2) {
                return new Integer(o1.getOffset()).compareTo(o2.getOffset());
            }
        });
        checkCorrectStartingIndexes(list, startIndexes);
    }

    private void checkCorrectStartingIndexes(final List<SourceRange> ranges, final int... startIndexes) {
        assertEquals("unexpected number of matching ranges:", startIndexes.length, ranges.size());
        for (int i = 0; i < ranges.size(); i++) {
            final SourceRange range = ranges.get(i);
            assertEquals(startIndexes[i], range.getOffset());
        }
    }

    private void checkCorrectLength(final Collection<SourceRange> ranges, final int length) {
        for (final SourceRange range : ranges) {
            assertEquals(length, range.getLength());
        }
    }
}
