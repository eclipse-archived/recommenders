/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.models.DependencyType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class ModelIndexBundleSymbolicNameAdvisorTest {

    private static final ProjectCoordinate COORDINATE = new ProjectCoordinate("example", "example.project", "1.0.0");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File exampleFile;

    @Before
    public void init() throws IOException {
        exampleFile = folder.newFile("example.jar");
    }

    @Test
    public void testUnsupportedDependencyType() {
        ModelIndexBundleSymbolicNameAdvisor sut = new ModelIndexBundleSymbolicNameAdvisor(null);
        assertThat(sut.suggest(new DependencyInfo(exampleFile, PROJECT)), is(Optional.<ProjectCoordinate>absent()));
    }

    @Test
    public void testValidJAR() throws IOException {
        DependencyInfo dependencyInfo = new DependencyInfo(exampleFile, JAR);

        IModelIndex mockedIndexer = mock(ModelIndex.class);
        when(mockedIndexer.suggestProjectCoordinateByArtifactId(COORDINATE.getArtifactId())).thenReturn(of(COORDINATE));

        OsgiManifestAdvisor mockedOsgiAdvisor = spy(new OsgiManifestAdvisor());
        when(mockedOsgiAdvisor.doSuggest(dependencyInfo)).thenReturn(of(COORDINATE));

        ModelIndexBundleSymbolicNameAdvisor sut = new ModelIndexBundleSymbolicNameAdvisor(mockedIndexer,
                mockedOsgiAdvisor);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(dependencyInfo);

        assertThat(optionalProjectCoordinate.get(), is(COORDINATE));
    }

    @Test
    public void testInvalidJAR() throws IOException {
        DependencyInfo dependencyInfo = new DependencyInfo(exampleFile, JAR);

        IModelIndex mockedIndexer = mock(ModelIndex.class);
        when(mockedIndexer.suggestProjectCoordinateByArtifactId(COORDINATE.getArtifactId())).thenReturn(
                Optional.<ProjectCoordinate>absent());

        OsgiManifestAdvisor mockedOsgiAdvisor = spy(new OsgiManifestAdvisor());
        when(mockedOsgiAdvisor.doSuggest(dependencyInfo)).thenReturn(of(COORDINATE));

        ModelIndexBundleSymbolicNameAdvisor sut = new ModelIndexBundleSymbolicNameAdvisor(mockedIndexer,
                mockedOsgiAdvisor);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(dependencyInfo);

        assertThat(optionalProjectCoordinate, is(Optional.<ProjectCoordinate>absent()));
    }
}
