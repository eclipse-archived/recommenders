package org.eclipse.recommenders.tests.completion.rcp.subwords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsProposalContext;
import org.junit.Test;
import org.mockito.Mockito;

public class SubwordsProposalContextTest {

    @Test
    public void test() {
        int s1 = calculateRelevance("none", "None", 325);
        int s2 = calculateRelevance("none", "NONE", 325);
        int s3 = calculateRelevance("none", "TRAVERSE_NONE", 412);
        assertEquals(s1, s2);
        assertTrue(s1 > s3);
    }

    private int calculateRelevance(String token, String completion, int score) {
        SubwordsProposalContext ctx = new SubwordsProposalContext(token, createCoreProposal(completion),
                createUiProposal(completion, score), Mockito.mock(JavaContentAssistInvocationContext.class));
        return ctx.calculateRelevance();
    }

    private IJavaCompletionProposal createUiProposal(String completion, int score) {
        IJavaCompletionProposal mock = Mockito.mock(IJavaCompletionProposal.class);
        Mockito.when(mock.getDisplayString()).thenReturn(completion);
        Mockito.when(mock.getRelevance()).thenReturn(score);
        return mock;
    }

    private CompletionProposal createCoreProposal(String completion) {
        CompletionProposal mock = Mockito.mock(CompletionProposal.class);
        Mockito.when(mock.getCompletion()).thenReturn(completion.toCharArray());
        return mock;
    }
}
