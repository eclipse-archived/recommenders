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

import java.util.Collections;

import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisScopeBuilder;
import org.junit.Ignore;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAOptions;

@Ignore
public class TestdataAnalysisFixtureModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IAnalysisScopeBuilder.class).to(TestdataAnalysisScopeBuilder.class);
    }

    @Provides
    @Singleton
    protected AnalysisScope provideAnalysisScope(final IAnalysisScopeBuilder builder) {
        return builder.buildPrimordialModules().buildApplicationModules().buildDependencyModules().buildExclusions()
                .getAnalysisScope();
    }

    @Provides
    @Singleton
    @SuppressWarnings("unchecked")
    protected AnalysisOptions provideAnalysisOptions(final AnalysisScope scope) {
        final AnalysisOptions options = new AnalysisOptions(scope, Collections.EMPTY_SET);
        options.setMaxNumberOfNodes(5000);
        options.setHandleStaticInit(false);
        options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());
        return options;
    }

    @Provides
    @Singleton
    protected IClassHierarchy provideClassHierarchy(final AnalysisScope scope) throws ClassHierarchyException {
        System.out.println("Start creating ClassHierarchy.");
        final ClassLoaderFactory factory = new ClassLoaderFactoryImpl(scope.getExclusions());
        final ClassHierarchy cha = ClassHierarchy.make(scope, factory);
        System.out.println("Loading ClassHierarchy finished.");
        return cha;
    }
}
