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

import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.eclipse.recommenders.tests.internal.completion.rcp.subwords.SubwordsMockUtils.mockJdtCompletion;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsJavaMethodCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;
import org.junit.Before;
import org.junit.Test;

public class RegexMatcherTest {

    private static final String c1 = "setFilters(ViewerFilter[] filters) : void - StructuredViewer";
    private SubwordsJavaMethodCompletionProposal sut;

    @Before
    public void before() throws JavaModelException {
        sut = createJavaCompletionProposal();
    }

    @Test
    public void testLowerCaseRegexMatch() {
        assertTrue(sut.isPrefix("strs", c1));
    }

    @Test
    public void testEmptyPrefixInput() {
        assertTrue(sut.isPrefix("", c1));
    }

    @Test
    public void testExactMatch() {
        assertTrue(sut.isPrefix("setFilters", c1));
    }

    @Test
    public void testExactMatchButLowerCase() {
        assertTrue(sut.isPrefix("setfilters", c1));
    }

    @Test
    public void testPrefixMatchButLowerCase() {
        assertTrue(sut.isPrefix("setf", c1));
    }

    @Test
    public void testExactMatchPlusOneChar() {
        assertFalse(sut.isPrefix("setFilters1", c1));
    }

    @Test
    public void testUpperCaseLetterMatch() {
        assertTrue(sut.isPrefix("sFi", c1));
    }

    @Test
    public void testUpperCaseLetterFail() {
        assertFalse(sut.isPrefix("sLt", c1));
    }

    @Test
    public void testReturnTypeInProposalIgnored() {
        assertFalse(sut.isPrefix("void", c1));
    }

    private SubwordsJavaMethodCompletionProposal createJavaCompletionProposal() throws JavaModelException {
        final CompletionProposal dummyProposal = mockCompletionProposal(CompletionProposal.METHOD_REF, c1);
        final JavaContentAssistInvocationContext dummyInvocationContext = mockInvocationContext();

        final SubwordsProposalContext subwordsContext = new SubwordsProposalContext("", dummyProposal,
                mockJdtCompletion(c1), dummyInvocationContext);
        final SubwordsJavaMethodCompletionProposal someSubwordsProposal = SubwordsJavaMethodCompletionProposal
                .create(subwordsContext);
        return someSubwordsProposal;
    }

}
