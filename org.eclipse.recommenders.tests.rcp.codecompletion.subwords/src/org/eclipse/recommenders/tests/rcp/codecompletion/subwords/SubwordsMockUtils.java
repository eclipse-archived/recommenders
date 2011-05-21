package org.eclipse.recommenders.tests.rcp.codecompletion.subwords;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

public class SubwordsMockUtils {

    public static CompletionProposal mockCompletionProposal() {
        return mock(CompletionProposal.class);
    }

    public static CompletionProposal mockMethodRefCompletionProposal(final String completion) {
        return mockCompletionProposal(CompletionProposal.METHOD_REF, completion);
    }

    public static CompletionProposal mockCompletionProposal(final int proposalKind, final String completion) {
        final CompletionProposal mock = mockCompletionProposal();
        when(mock.getKind()).thenReturn(proposalKind);
        when(mock.getCompletion()).thenReturn(completion.toCharArray());
        return mock;
    }

    public static JavaContentAssistInvocationContext mockInvocationContext() {
        final JavaContentAssistInvocationContext javaContext = mock(JavaContentAssistInvocationContext.class);
        final CompletionContext completionContext = mock(CompletionContext.class);
        when(javaContext.getCoreContext()).thenReturn(completionContext);
        return javaContext;
    }
}
