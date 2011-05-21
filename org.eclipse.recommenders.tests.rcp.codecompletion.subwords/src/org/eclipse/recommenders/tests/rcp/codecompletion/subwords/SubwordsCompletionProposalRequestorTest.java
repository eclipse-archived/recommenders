package org.eclipse.recommenders.tests.rcp.codecompletion.subwords;

import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_BLOCK_TAG;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.recommenders.tests.rcp.codecompletion.subwords.SubwordsMockUtils.mockCompletionProposal;
import static org.eclipse.recommenders.tests.rcp.codecompletion.subwords.SubwordsMockUtils.mockInvocationContext;
import static org.eclipse.recommenders.tests.rcp.codecompletion.subwords.SubwordsMockUtils.mockMethodRefCompletionProposal;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.rcp.codecompletion.subwords.SubwordsCompletionRequestor;
import org.junit.Test;

public class SubwordsCompletionProposalRequestorTest {;


    @Test
    public void testHappyPath() {
        // setup:
        final CompletionProposal proposal = mockMethodRefCompletionProposal("completion(...)");
        final SubwordsCompletionRequestor sut = createSut("cmp");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(1,sut);
    }

    @Test
    public void testRightProposalKindButNoCompletionMatch() {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(METHOD_REF, "completion(...)");
        final SubwordsCompletionRequestor sut = createSut("moc");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(0,sut);
    }

    @Test
    public void testCompletionMatchButWrongProposalKind() {
        // setup:
        final CompletionProposal proposal = mockCompletionProposal(JAVADOC_BLOCK_TAG, "completion(...)");
        final SubwordsCompletionRequestor sut = createSut("cmpl");
        // exercise:
        sut.accept(proposal);
        // verify:
        assertNumberOfAcceptedProposals(0,sut);
    }
    
    private void assertNumberOfAcceptedProposals(final int expectedNumberOfProposals, final SubwordsCompletionRequestor requestor) {
        final List<IJavaCompletionProposal> acceptedProposals = requestor.getProposals();
        assertEquals("proposal requestor did not accept what you expected :(",expectedNumberOfProposals, acceptedProposals.size());
    }


    private SubwordsCompletionRequestor createSut(final String token) {
        return new SubwordsCompletionRequestor(token, mockInvocationContext());
    }
}
