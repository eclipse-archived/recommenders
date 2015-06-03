package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.recommenders.snipmatch.Location;
import org.junit.Test;

public class JavaEditorSearchContextText {

    @Test
    public void testGetJavadocLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaEditorSearchContext.getLocation(context, IJavaPartitions.JAVA_DOC);

        assertThat(location, is(Location.JAVADOC));
    }

    @Test
    public void testGetSingleLineCommentLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaEditorSearchContext.getLocation(context, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);

        assertThat(location, is(Location.FILE));
    }

    @Test
    public void testGetMultiLineCommentLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaEditorSearchContext.getLocation(context, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);

        assertThat(location, is(Location.FILE));
    }

    @Test
    public void testGetJavaTypeMembersLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TL_MEMBER_START);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaEditorSearchContext.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_TYPE_MEMBERS));
    }

    @Test
    public void testGetJavaStatementsLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TL_STATEMENT_START);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaEditorSearchContext.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_STATEMENTS));
    }

    @Test
    public void testUnknownTokenLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TOKEN_KIND_UNKNOWN);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaEditorSearchContext.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.UNKNOWN));
    }

    @Test
    public void testCoreContextUnavailable() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaEditorSearchContext.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.FILE));
    }

    @Test
    public void testUnknownPartition() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaEditorSearchContext.getLocation(context, "unknown");

        assertThat(location, is(Location.FILE));
    }
}
