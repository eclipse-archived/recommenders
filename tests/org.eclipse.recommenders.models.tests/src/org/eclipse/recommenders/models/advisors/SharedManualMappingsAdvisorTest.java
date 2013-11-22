/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.tests.models.utils.FolderUtils.dir;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class SharedManualMappingsAdvisorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File mappingsFile;

    private SharedManualMappingsAdvisor sut;

    @Before
    public void setUp() throws IOException {
        mappingsFile = temp.newFile();
        IModelRepository repository = mock(IModelRepository.class);
        when(repository.resolve(eq(SharedManualMappingsAdvisor.MAPPINGS), anyBoolean())).thenReturn(
                Optional.of(mappingsFile));

        sut = new SharedManualMappingsAdvisor(repository);
    }

    @Test
    public void testEmptyMappingsFile() throws IOException {
        appendLine("");

        Optional<ProjectCoordinate> suggestion = sut.doSuggest(jarDependency(dir("home", "user", "workspace",
                "project", "lib", "example.jar")));

        assertThat(suggestion.isPresent(), is(false));
    }

    @Test
    public void testSingleEntryMappingsFile() throws IOException {
        appendLine("example.jar=org.example:example:1.0.0");

        Optional<ProjectCoordinate> suggestion = sut.doSuggest(jarDependency(dir("home", "user", "workspace",
                "project", "lib", "example.jar")));

        assertThat(suggestion.get(), is(equalTo(ProjectCoordinate.valueOf("org.example:example:1.0.0"))));
    }

    @Test
    public void testMultiEntryMappingsFile() throws IOException {
        appendLine("first.jar=org.example:first:1.0.0");
        appendLine("second.jar=org.example:second:2.0.0");

        Optional<ProjectCoordinate> suggestion = sut
                .doSuggest(jarDependency(dir("home", "user", "workspace", "project", "lib", "second.jar")));

        assertThat(suggestion.get(), is(equalTo(ProjectCoordinate.valueOf("org.example:second:2.0.0"))));
    }

    @Test
    public void testMismatch() throws IOException {
        appendLine("mismatch.jar=org.example:mismatch:1.0.0");

        Optional<ProjectCoordinate> suggestion = sut
                .doSuggest(jarDependency(dir("home", "user", "workspace", "project", "lib", "example.jar")));

        assertThat(suggestion.isPresent(), is(false));
    }

    @Test
    public void testFirstMatchWins() throws IOException {
        appendLine("*.jar=org.example:any:0.0.0");
        appendLine("example.jar=org.example:example:1.0.0");

        Optional<ProjectCoordinate> suggestion = sut
                .doSuggest(jarDependency(dir("home", "user", "workspace", "project", "lib", "example.jar")));

        assertThat(suggestion.get(), is(equalTo(ProjectCoordinate.valueOf("org.example:any:0.0.0"))));
    }

    private void appendLine(String line) throws IOException {
        append(line + '\n', mappingsFile, UTF_8);
    }

    private DependencyInfo jarDependency(File file) {
        return new DependencyInfo(file, JAR);
    }
}
