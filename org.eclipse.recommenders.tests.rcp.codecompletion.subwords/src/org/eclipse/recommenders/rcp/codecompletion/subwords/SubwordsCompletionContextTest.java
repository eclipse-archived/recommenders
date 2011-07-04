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
package org.eclipse.recommenders.rcp.codecompletion.subwords;

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
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsMockUtils.mockJdtCompletion;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.junit.Test;

public class SubwordsCompletionContextTest {

    @Test
    public void testHighlights01() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("kk", "kkokk");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 0);
        checkCorrectStartingIndexes(regexMatches, 0, 1);
    }

    @Test
    public void testHighlights03() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("lotat", "setLayoutData");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 11, 10);
        checkCorrectStartingIndexes(regexMatches, 3, 6, 8, 10, 11);
    }

    @Test
    public void testHighlights02() throws JavaModelException {

        final SubwordsProposalContext sut = createTestContext("tken", "token()");
        // exercise:
        final List<SourceRange> bigramMatches = sut.findBigramHighlightRanges();
        final List<SourceRange> regexMatches = sut.findRegexHighlightRanges();
        // verify:
        checkCorrectStartingIndexes(bigramMatches, 2, 3);
        checkCorrectStartingIndexes(regexMatches, 0, 2, 3, 4);
    }

    private SubwordsProposalContext createTestContext(final String prefix, final String completion)
            throws JavaModelException {
        final SubwordsProposalContext sut = new SubwordsProposalContext(prefix, mockCompletionProposal(),
                mockJdtCompletion(completion), mockInvocationContext());
        return sut;
    }

    private void checkCorrectStartingIndexes(final List<SourceRange> ranges, final int... startIndexes) {
        assertEquals("unexpected number of matching ranges:", startIndexes.length, ranges.size());
        for (int i = 0; i < ranges.size(); i++) {
            assertEquals(startIndexes[i], ranges.get(i).getOffset());
        }
    }
}
