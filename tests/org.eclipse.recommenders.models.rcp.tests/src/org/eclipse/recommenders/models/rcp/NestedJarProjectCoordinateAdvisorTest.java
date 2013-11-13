package org.eclipse.recommenders.models.rcp;

import static org.eclipse.recommenders.models.DependencyInfo.SURROUNDING_PROJECT_FILE;
import static org.eclipse.recommenders.models.DependencyType.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.models.rcp.NestedJarProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

public class NestedJarProjectCoordinateAdvisorTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private NestedJarProjectCoordinateAdvisor sut;

    @Before
    public void init() {
        sut = new NestedJarProjectCoordinateAdvisor();
        sut.pcProvider = mock(IProjectCoordinateProvider.class);
    }

    @Test
    public void testCallWithOutHintReturnsAbsent() throws IOException {
        DependencyInfo di = new DependencyInfo(folder.newFile(), JAR);

        assertFalse(sut.suggest(di).isPresent());
    }

    @Test
    public void testCallWithOutHintIsNotPropagated() throws IOException {
        DependencyInfo di = new DependencyInfo(folder.newFile(), JAR);

        sut.suggest(di);

        verify(sut.pcProvider, never()).resolve(any(DependencyInfo.class));
    }

    @Test
    public void testCallWithEmptyFileHintReturnsAbsent() throws IOException {
        DependencyInfo di = new DependencyInfo(folder.newFile(), JAR, ImmutableMap.of(SURROUNDING_PROJECT_FILE, ""));

        assertFalse(sut.suggest(di).isPresent());
    }

    @Test
    public void testCallWithEmptyFileHintIsNotPropagated() throws IOException {
        DependencyInfo di = new DependencyInfo(folder.newFile(), JAR, ImmutableMap.of(SURROUNDING_PROJECT_FILE, ""));

        sut.suggest(di);

        verify(sut.pcProvider, never()).resolve(any(DependencyInfo.class));
    }

    @Test
    public void testCallWithHintIsPropagated() throws IOException {
        File projectFile = folder.newFolder("TestProject");

        DependencyInfo di = new DependencyInfo(folder.newFile(), DependencyType.JAR, ImmutableMap.of(
                SURROUNDING_PROJECT_FILE, projectFile.getAbsolutePath()));

        sut.suggest(di);

        verify(sut.pcProvider, times(1)).resolve(new DependencyInfo(projectFile, PROJECT));
    }

}
