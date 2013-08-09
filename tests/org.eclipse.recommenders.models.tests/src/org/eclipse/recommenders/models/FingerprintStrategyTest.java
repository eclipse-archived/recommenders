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
package org.eclipse.recommenders.models;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.advisors.FingerprintAdvisor;
import org.eclipse.recommenders.utils.Fingerprints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class FingerprintStrategyTest {

    private static final ProjectCoordinate COORDINATE = new ProjectCoordinate("example", "example.project", "1.0.0");
    private static final ProjectCoordinate COORDINATE1 = new ProjectCoordinate("example", "", "1.0.0");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File exampleFile;

    @Before
    public void init() throws IOException {
        exampleFile = folder.newFile("example.jar");
    }

    @Test
    public void testInvalidDependencyType() {
        FingerprintAdvisor sut = new FingerprintAdvisor(null);
        sut.suggest(new DependencyInfo(exampleFile, DependencyType.PROJECT));
    }

    @Test
    public void testValidJAR() throws IOException {
        IModelIndex mockedIndexer = mock(ModelIndex.class);
        when(mockedIndexer.suggestProjectCoordinateByFingerprint(Fingerprints.sha1(exampleFile))).thenReturn(
                Optional.fromNullable(COORDINATE));

        FingerprintAdvisor sut = new FingerprintAdvisor(mockedIndexer);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.suggest(new DependencyInfo(exampleFile,
                DependencyType.JAR));

        Assert.assertEquals(COORDINATE, optionalProjectCoordinate.get());
    }
}
