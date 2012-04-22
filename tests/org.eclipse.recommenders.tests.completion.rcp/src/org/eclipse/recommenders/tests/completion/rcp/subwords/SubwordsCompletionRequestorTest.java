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
package org.eclipse.recommenders.tests.completion.rcp.subwords;

import static org.eclipse.jdt.core.CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_BLOCK_TAG;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_REF;
import static org.eclipse.recommenders.tests.completion.rcp.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.tests.completion.rcp.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionRequestor;
import org.junit.Test;

public class SubwordsCompletionRequestorTest {

    @Test
    public void testHappyPath() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(METHOD_REF, "completion()");
        final SubwordsCompletionRequestor sut = createSut("cmp");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    @Test
    public void testRightProposalKindButNoCompletionMatch() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(METHOD_REF, "completion(...)");
        final SubwordsCompletionRequestor sut = createSut("moc");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(0, sut);
    }

    @Test
    public void testCompletionMatchButWrongProposalKind() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(JAVADOC_BLOCK_TAG, "completion(...)");
        final SubwordsCompletionRequestor sut = createSut("cmpl");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    @Test
    public void testTypeCompletion() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(TYPE_REF, "Text(...)");
        final SubwordsCompletionRequestor sut = createSut("te");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    @Test
    public void testFieldCompletion() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(FIELD_REF_WITH_CASTED_RECEIVER, "field");
        final SubwordsCompletionRequestor sut = createSut("ld");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    @Test
    public void testOverrideCompletion() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(METHOD_DECLARATION, "createControl");
        final SubwordsCompletionRequestor sut = createSut("cont");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    @Test
    public void testAnonymousTypeCompletion() throws JavaModelException {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
                "TestClass");
        final SubwordsCompletionRequestor sut = createSut("tclas");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1, sut);
    }

    private void assertNumberOfAcceptedProposals(final int expectedNumberOfProposals,
            final SubwordsCompletionRequestor requestor) {
        final List<IJavaCompletionProposal> acceptedProposals = requestor.getProposals();
        assertEquals("proposal requestor did not accept what you expected :(", expectedNumberOfProposals,
                acceptedProposals.size());
    }

    private SubwordsCompletionRequestor createSut(final String token) throws JavaModelException {
        return new SubwordsCompletionRequestor(token, mockInvocationContext()) {
            @Override
            protected boolean shouldFillArgumentNames() {
                return false;
            }
        };
    }
}
