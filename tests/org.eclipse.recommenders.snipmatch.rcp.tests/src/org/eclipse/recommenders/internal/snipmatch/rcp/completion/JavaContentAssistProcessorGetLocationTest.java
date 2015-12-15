package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.recommenders.snipmatch.Location;
import org.junit.Test;

public class JavaContentAssistProcessorGetLocationTest {

    @Test
    public void testGetLocationInJavadoc() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaContentAssistProcessor.getLocation(context, IJavaPartitions.JAVA_DOC);

        assertThat(location, is(Location.JAVADOC));
    }

    @Test
    public void testGetLocationInSingleLineComment() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaContentAssistProcessor.getLocation(context, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);

        assertThat(location, is(Location.JAVA_FILE));
    }

    @Test
    public void testGetLocationInMultiLineComment() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaContentAssistProcessor.getLocation(context, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);

        assertThat(location, is(Location.JAVA_FILE));
    }

    @Test
    public void testGetLocationAtJavaTypeMemberStart() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TL_MEMBER_START);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaContentAssistProcessor.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_TYPE_MEMBERS));
    }

    @Test
    public void testGetLocationAtJavaStatementStart() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TL_STATEMENT_START);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaContentAssistProcessor.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_STATEMENTS));
    }

    @Test
    public void testUnknownTokenLocation() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);
        CompletionContext coreContext = mock(CompletionContext.class);
        when(coreContext.getTokenLocation()).thenReturn(CompletionContext.TOKEN_KIND_UNKNOWN);
        when(context.getCoreContext()).thenReturn(coreContext);

        Location location = JavaContentAssistProcessor.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_FILE));
    }

    @Test
    public void testCoreContextUnavailable() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaContentAssistProcessor.getLocation(context, IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(location, is(Location.JAVA_FILE));
    }

    @Test
    public void testUnknownPartition() {
        JavaContentAssistInvocationContext context = mock(JavaContentAssistInvocationContext.class);

        Location location = JavaContentAssistProcessor.getLocation(context, "unknown");

        assertThat(location, is(Location.JAVA_FILE));
    }
}
