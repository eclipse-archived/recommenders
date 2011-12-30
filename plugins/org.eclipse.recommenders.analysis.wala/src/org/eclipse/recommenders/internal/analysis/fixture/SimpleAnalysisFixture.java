/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.analysis.fixture;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.internal.analysis.utils.WalaUtils;

import com.google.common.collect.Sets;

public class SimpleAnalysisFixture implements IAnalysisFixture {

    private final SimpleAnalysisFixtureConfiguration configuration;
    private HashSet<File> javaRuntime;

    public static SimpleAnalysisFixture create(final SimpleAnalysisFixtureConfiguration configuration) {
        return new SimpleAnalysisFixture(configuration);
    }

    private SimpleAnalysisFixture(final SimpleAnalysisFixtureConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return configuration.name;
    }

    @Override
    public String getDescription() {
        return configuration.description;
    }

    @Override
    public Set<File> getApplication() {
        return configuration.application;

    }

    @Override
    public Set<File> getJavaRuntime() {
        initalizeJavaRuntimeOnce();
        return javaRuntime;
    }

    private void initalizeJavaRuntimeOnce() {
        if (javaRuntime == null) {
            final File javaHome = SystemUtils.getJavaHome();
            final Collection<File> jars = WalaUtils.getAllJarsInDirectoryRecursively(javaHome);
            javaRuntime = Sets.newHashSet(jars);
        }
    }

    @Override
    public Set<File> getDependencies() {
        return configuration.dependencies;
    }

}
