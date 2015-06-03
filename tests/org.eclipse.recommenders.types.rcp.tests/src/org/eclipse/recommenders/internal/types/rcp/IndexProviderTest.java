package org.eclipse.recommenders.internal.types.rcp;

import static java.io.File.separatorChar;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.IJavaProject;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class IndexProviderTest {

    @Test
    public void testSimpleName() {
        IJavaProject project = mockProject("Project");

        String actual = IndexProvider.ProjectTypesIndexCacheLoader.computeIndexDir(project).getAbsolutePath();
        String expected = constructPath("Project");

        assertThat(actual, Matchers.endsWith(expected));
    }

    @Test
    public void testMangledName() {
        IJavaProject project = mockProject("Project with\tWhitespace");

        String actual = IndexProvider.ProjectTypesIndexCacheLoader.computeIndexDir(project).getAbsolutePath();

        String expected = constructPath("Project_with_Whitespace");
        assertThat(actual, Matchers.endsWith(expected));
    }

    private IJavaProject mockProject(String name) {
        IJavaProject project = mock(IJavaProject.class);
        when(project.getElementName()).thenReturn(name);
        return project;
    }

    private String constructPath(String mangledName) {
        StringBuilder sb = new StringBuilder();
        sb.append(".metadata").append(separatorChar).append(".plugins").append(separatorChar);
        Bundle bundle = FrameworkUtil.getBundle(IndexProvider.class);
        sb.append(bundle.getSymbolicName()).append(separatorChar);
        sb.append(IndexProvider.INDEX_DIR).append(separatorChar);
        sb.append(mangledName);
        return sb.toString();
    }
}
