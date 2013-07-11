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
package org.eclipse.recommenders.tests.models;

import static com.google.common.base.Optional.fromNullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.impl.FingerprintStrategy;
import org.eclipse.recommenders.models.dependencies.impl.SimpleIndexSearcher;
import org.eclipse.recommenders.utils.Fingerprints;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class FingerprintStrategyTest {

    private static final ProjectCoordinate EXPECTED_PROJECT_COORDINATE = new ProjectCoordinate("example",
            "example.project", "1.0.0");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testInvalidDependencyType() {
        FingerprintStrategy sut = new FingerprintStrategy(null);
        sut.searchForProjectCoordinate(new DependencyInfo(null, DependencyType.PROJECT));
    }

    @Test
    public void testValidJAR() throws IOException {
        File jar = folder.newFile("example.jar");

        SimpleIndexSearcher mockedIndexer = mock(SimpleIndexSearcher.class);
        when(mockedIndexer.searchByFingerprint(Fingerprints.sha1(jar))).thenReturn(
                Optional.fromNullable("example:example.project:jar:1.0.0"));

        FingerprintStrategy sut = new FingerprintStrategy(mockedIndexer);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(jar,
                DependencyType.JAR));

        Assert.assertEquals(EXPECTED_PROJECT_COORDINATE, optionalProjectCoordinate.get());
    }

    @Test
    public void testMissingInformation() throws IOException {
        File jar = folder.newFile("example.jar");

        SimpleIndexSearcher mockedIndexer = mock(SimpleIndexSearcher.class);
        when(mockedIndexer.searchByFingerprint(Fingerprints.sha1(jar))).thenReturn(fromNullable("example:1.0.0"));

        FingerprintStrategy sut = new FingerprintStrategy(mockedIndexer);
        Optional<ProjectCoordinate> optionalProjectCoordinate = sut.searchForProjectCoordinate(new DependencyInfo(jar,
                DependencyType.JAR));

        Assert.assertFalse(optionalProjectCoordinate.isPresent());
    }

}
