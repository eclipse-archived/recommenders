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
package org.eclipse.recommenders.tests.internal.completion.rcp.subwords;

import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext.PREFIX_BONUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("restriction")
@Ignore("Relevance Calculation has been removed from implementation")
public class SubwordsRelevanceCalculationTest {

    private static SubwordsProposalContext createSut(final String token, final String completion)
            throws JavaModelException {
        final CompletionProposal proposal = SubwordsMockUtils.mockCompletionProposal(CompletionProposal.METHOD_REF,
                completion);
        final JavaCompletionProposal jdtProposal = SubwordsMockUtils.mockJdtCompletion(completion);
        final JavaContentAssistInvocationContext context = SubwordsMockUtils.mockInvocationContext(token);
        final SubwordsProposalContext sut = new SubwordsProposalContext(token, proposal, jdtProposal, context);
        return sut;
    }

    @Test
    public void testEmptyToken() throws JavaModelException {
        final SubwordsProposalContext sut = createSut("", "someMethod");
        assertTrue(sut.isRegexMatchButNoPrefixMatch());
        assertTrue(sut.isPrefixMatch());
        assertEquals(PREFIX_BONUS, sut.calculateRelevance());
    }

    @Test
    public void testPrefixToken() throws JavaModelException {
        final SubwordsProposalContext sut = createSut("set", "setText");
        assertTrue(sut.isRegexMatchButNoPrefixMatch());
        assertTrue(sut.isPrefixMatch());
        assertEquals(PREFIX_BONUS + 2, sut.calculateRelevance());
    }

    @Test
    public void testSubword() throws JavaModelException {
        final SubwordsProposalContext sut = createSut("text", "setText");
        assertTrue(sut.isRegexMatchButNoPrefixMatch());
        assertFalse(sut.isPrefixMatch());
        assertEquals(3, sut.calculateRelevance());
    }

    @Test
    public void testIrrelevantCompletion() throws JavaModelException {
        final SubwordsProposalContext sut = createSut("get", "setText");
        assertFalse(sut.isRegexMatchButNoPrefixMatch());
    }
}
