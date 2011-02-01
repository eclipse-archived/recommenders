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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsDirectory;

import java.io.File;
import java.util.jar.JarFile;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultExclusionSetOfClass;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaUtils;
import org.junit.Ignore;

import com.google.inject.Inject;
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;

@Ignore
public class TestdataAnalysisScopeBuilder implements IAnalysisScopeBuilder {

    private File testdataHome = new File("../org.eclipse.recommenders.tests.commons.data/target/classes/")
            .getAbsoluteFile();

    private final AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

    @Inject
    public TestdataAnalysisScopeBuilder() {
        ensureIsDirectory(testdataHome);
    }

    public TestdataAnalysisScopeBuilder(final File testdataHome) {
        this.testdataHome = testdataHome;
    }

    @Override
    public IAnalysisScopeBuilder buildPrimordialModules() {
        final File javaHome = SystemUtils.getJavaHome();
        for (final File jarFile : WalaUtils.getAllJarsInDirectory(javaHome, true)) {
            try {
                final JarFile jar = new JarFile(jarFile);
                scope.addToScope(ClassLoaderReference.Primordial, jar);
            } catch (final Exception e) {
                e.printStackTrace();
                // throwUnhandledException(e);
            }
        }
        return this;
    }

    @Override
    public IAnalysisScopeBuilder buildApplicationModules() {
        final BinaryDirectoryTreeModule module = new BinaryDirectoryTreeModule(testdataHome);
        scope.addToScope(ClassLoaderReference.Application, module);
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
    public IAnalysisScopeBuilder buildDependencyModules() {
        return this;
    }
}
