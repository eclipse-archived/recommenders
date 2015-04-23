package org.eclipse.recommenders.completion.rcp.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions;
import org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class IntelligentProposalComputerTest {

    private IntelligentCompletionProposalComputer sut;
    private List<ICompletionProposal> NO_PROPOSALS = Collections.<ICompletionProposal>emptyList();

    @Before
    public void before() {
        CompletionRcpPreferences preferences = new CompletionRcpPreferences();
        preferences.setEnabledSessionProcessorString("");
        sut = new IntelligentCompletionProposalComputer(preferences, new CachingAstProvider(), new SharedImages(),
                CompletionContextFunctions.defaultFunctions());

    }

    @Test
    public void testNullProject() {
        // setup
        ICompilationUnit cu = mock(ICompilationUnit.class);
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(cu);

        // exercise
        sut.sessionStarted();
        List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);

        // verify
        Assert.assertThat(proposals, equalTo(NO_PROPOSALS));
    }

    @Test
    public void testProjectExistsFalse() {
        // setup
        ICompilationUnit cu = mock(ICompilationUnit.class);
        IJavaProject project = mock(IJavaProject.class);
        when(cu.getJavaProject()).thenReturn(project);
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(cu);

        // exercise
        sut.sessionStarted();
        List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);

        // verify
        Assert.assertThat(proposals, equalTo(NO_PROPOSALS));
    }

    @Test(expected = AssertionFailedException.class)
    public void testProjectExistsTrue() {
        // setup
        ICompilationUnit cu = mock(ICompilationUnit.class);
        IJavaProject project = mock(IJavaProject.class);
        when(cu.getJavaProject()).thenReturn(project);
        when(project.exists()).thenReturn(true);
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(cu);

        // exercise
        sut.sessionStarted();
        sut.computeCompletionProposals(ctx, null);

        // verify. We expect that we receive an assertion failed exception caused elsewhere
        // this is not clean but at least ensures that we do not fail silently early...
        fail("should not come that far");
    }

}
