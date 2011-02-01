/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.fixture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.inject.Inject;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;

public class DefaultAnalysisScopeBuilder implements IAnalysisScopeBuilder {
    private final IAnalysisFixture fixture;

    private final AnalysisScope scope;

    public static IAnalysisScopeBuilder buildFromFixture(final IAnalysisFixture fixture) {
        return new DefaultAnalysisScopeBuilder(fixture).buildPrimordialModules().buildApplicationModules()
                .buildDependencyModules().buildExclusions();
    }

    @Inject
    public DefaultAnalysisScopeBuilder(final IAnalysisFixture fixture) {
        this.fixture = checkNotNull(fixture);
        scope = AnalysisScope.createJavaAnalysisScope();
    }

    @Override
    public IAnalysisScopeBuilder buildPrimordialModules() {
        for (final File stdlib : fixture.getJavaRuntime()) {
            addJarToClassLoaderRef(stdlib, ClassLoaderReference.Primordial);
        }
        return this;
    }

    private void addJarToClassLoaderRef(final File path, final ClassLoaderReference classloader) {
        try {
            final JarFile jarFile = new JarFile(path);
            final Module module = new JarFileModule(jarFile);
            scope.addToScope(classloader, module);
        } catch (final IOException e) {
            final String message = format("Failed to add '%s' to classloader '%s'. Reason: %s\n", path,
                    classloader.getName(), e.getMessage());
            System.err.printf(message);
            // we don't throw exceptions here because this happens quite often
            // throwUnhandledException(e);
        }
    }

    @Override
    public IAnalysisScopeBuilder buildApplicationModules() {
        for (final File jar : fixture.getApplication()) {
            addJarToApplicationScope(jar);
        }
        return this;
    }

    private void addJarToApplicationScope(final File path) {
        addJarToClassLoaderRef(path, ClassLoaderReference.Application);
    }

    private void addJarToExtensionsScope(final File path) {
        addJarToClassLoaderRef(path, ClassLoaderReference.Extension);
    }

    @Override
    public IAnalysisScopeBuilder buildDependencyModules() {
        for (final File jar : fixture.getDependencies()) {
            addJarToExtensionsScope(jar);
        }
        return this;
    }

    @Override
    public IAnalysisScopeBuilder buildExclusions() {
        scope.setExclusions(new DefaultExclusionSetOfClass());
        return this;
    }

    @Override
    public AnalysisScope getAnalysisScope() {
        return scope;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
