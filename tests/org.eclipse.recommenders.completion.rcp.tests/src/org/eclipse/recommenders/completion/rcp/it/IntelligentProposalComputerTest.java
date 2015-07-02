package org.eclipse.recommenders.completion.rcp.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions;
import org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.ui.IEditorPart;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public class IntelligentProposalComputerTest {

    private List<ICompletionProposal> NO_PROPOSALS = Collections.<ICompletionProposal>emptyList();

    public IntelligentCompletionProposalComputer createSUT() {
        return createSUT(new Provider<IEditorPart>() {

            @Override
            public IEditorPart get() {
                return (IEditorPart) new CompilationUnitEditor();
            }
        });
    }

    public IntelligentCompletionProposalComputer createSUT(Provider<IEditorPart> editorProvider) {
        CompletionRcpPreferences preferences = new CompletionRcpPreferences();
        preferences.setEnabledSessionProcessorString("");
        return new IntelligentCompletionProposalComputer(preferences, new CachingAstProvider(), new SharedImages(),
                CompletionContextFunctions.defaultFunctions(), editorProvider);
    }

    @Test
    public void testNullProject() {
        // setup
        ICompilationUnit cu = mock(ICompilationUnit.class);
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(cu);

        // exercise
        IntelligentCompletionProposalComputer sut = createSUT();
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
        IntelligentCompletionProposalComputer sut = createSUT();
        sut.sessionStarted();
        List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);

        // verify
        Assert.assertThat(proposals, equalTo(NO_PROPOSALS));
    }

    @Test
    public void testSubClassOfCompilationUnitEditor() {
        // setup
        ICompilationUnit cu = mock(ICompilationUnit.class);
        IJavaProject project = mock(IJavaProject.class);
        when(cu.getJavaProject()).thenReturn(project);
        when(project.exists()).thenReturn(true);
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(cu);

        Provider<IEditorPart> retriever = new Provider<IEditorPart>() {

            @Override
            public IEditorPart get() {
                CompilationUnitEditor nonJavaEditor = new CompilationUnitEditor() {
                };
                return (IEditorPart) nonJavaEditor;
            }
        };

        // exercise
        IntelligentCompletionProposalComputer sut = createSUT(retriever);
        sut.sessionStarted();
        List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);

        // verify
        Assert.assertThat(proposals, equalTo(NO_PROPOSALS));
    }
}
