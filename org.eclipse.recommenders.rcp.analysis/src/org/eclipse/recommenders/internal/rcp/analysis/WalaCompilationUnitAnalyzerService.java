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
package org.eclipse.recommenders.internal.rcp.analysis;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.CallGraphMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.CompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ConstructorSuperDeclarationMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredFieldsClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredInterfacesClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredSuperclassClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.FingerprintCompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.FirstDeclarationMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.IClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.IMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.JavaLangInstanceKeysRemoverCompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.LineNumberMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.LocalNamesCollectingCallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ModifiersClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ModifiersMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.NameClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.NameMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ParameterCallsitesCallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ReceiverCallsitesCallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.SuperDeclarationMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ThisObjectInstanceKeyCompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.WalaDefaultInstanceKeysRemoverCompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.AllMethodsAndContructorsEntrypointSelector;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.IEntrypointSelector;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
import com.ibm.wala.util.debug.UnimplementedError;

public class WalaCompilationUnitAnalyzerService implements ICompilationUnitAnalyzer<CompilationUnit> {
    private final IClassHierarchyService wala;

    private Injector injector;

    @Inject
    public WalaCompilationUnitAnalyzerService(final IClassHierarchyService wala) {
        this.wala = wala;
    }

    @Override
    @SuppressWarnings("unused")
    public CompilationUnit analyze(final ICompilationUnit jdtCompilationUnit, final IProgressMonitor monitor) {
        System.out.println("start analyzing " + jdtCompilationUnit.getElementName());
        final IType jdtType = jdtCompilationUnit.findPrimaryType();
        if (jdtType == null) {
            return null;
        }
        final IClass walaClass = wala.getType(jdtType);
        if (walaClass == null) {
            return null;
        }
        final CompilationUnit recCompilationUnit = CompilationUnit.create();
        //
        //
        createAnalysisInjector(walaClass);
        final WalaCompiliationUnitAnalzyer r = injector.getInstance(WalaCompiliationUnitAnalzyer.class);
        r.init(jdtCompilationUnit, walaClass, recCompilationUnit);
        try {
            r.run(monitor);
        } catch (final Exception x) {
            RcpAnalysisPlugin.logError(x, "error during analysis if '%s'", walaClass.getName());
        } catch (final UnimplementedError x) {
            RcpAnalysisPlugin.logError(x, "error during analysis if '%s'", walaClass.getName());
        }
        System.out.println("end analyzing " + jdtCompilationUnit.getElementName());

        return recCompilationUnit;
    }

    private void createAnalysisInjector(final IClass walaClass) {
        final Injector masterInjector = InjectionService.getInstance().getInjector();
        injector = masterInjector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                configureClassAnalyzer();
                configureMethodAnalyzer();
                configureCallgraphAnalyzer();
                configureCompilationUnitFinalizer();
                configureCompilationUnitConsumer();
                bind(AnalysisCache.class).toInstance(new AnalysisCache());
            }

            private void configureCompilationUnitConsumer() {
                // empty binder
                Multibinder.newSetBinder(binder(), ICompilationUnitConsumer.class);
            }

            private void configureClassAnalyzer() {
                final Multibinder<IClassAnalyzer> b = Multibinder.newSetBinder(binder(), IClassAnalyzer.class);
                b.addBinding().to(NameClassAnalyzer.class);
                b.addBinding().to(ModifiersClassAnalyzer.class);
                b.addBinding().to(DeclaredSuperclassClassAnalyzer.class);
                b.addBinding().to(DeclaredInterfacesClassAnalyzer.class);
                b.addBinding().to(DeclaredFieldsClassAnalyzer.class);

            }

            private void configureMethodAnalyzer() {
                final Multibinder<IMethodAnalyzer> b = Multibinder.newSetBinder(binder(), IMethodAnalyzer.class);
                b.addBinding().to(ModifiersMethodAnalyzer.class);
                b.addBinding().to(NameMethodAnalyzer.class);
                b.addBinding().to(LineNumberMethodAnalyzer.class);
                b.addBinding().to(FirstDeclarationMethodAnalyzer.class);
                b.addBinding().to(SuperDeclarationMethodAnalyzer.class);
                b.addBinding().to(ConstructorSuperDeclarationMethodAnalyzer.class);
                b.addBinding().to(CallGraphMethodAnalyzer.class);
            }

            private void configureCallgraphAnalyzer() {
                final Multibinder<ICallGraphAnalyzer> b = Multibinder.newSetBinder(binder(), ICallGraphAnalyzer.class);
                b.addBinding().to(ParameterCallsitesCallGraphAnalyzer.class);
                b.addBinding().to(ReceiverCallsitesCallGraphAnalyzer.class);
                b.addBinding().to(ReceiverCallsitesCallGraphAnalyzer.class);
                b.addBinding().to(LocalNamesCollectingCallGraphAnalyzer.class);

            }

            private void configureCompilationUnitFinalizer() {
                final Multibinder<CompilationUnitFinalizer> binder = Multibinder.newSetBinder(binder(),
                        CompilationUnitFinalizer.class);
                binder.addBinding().to(JavaLangInstanceKeysRemoverCompilationUnitFinalizer.class).in(Singleton.class);
                binder.addBinding().to(WalaDefaultInstanceKeysRemoverCompilationUnitFinalizer.class)
                        .in(Singleton.class);
                binder.addBinding().to(ThisObjectInstanceKeyCompilationUnitFinalizer.class).in(Singleton.class);
                binder.addBinding().to(FingerprintCompilationUnitFinalizer.class).in(Singleton.class);

            }

            @Provides
            @Singleton
            public IClassHierarchy provideClassHierarchy() {
                return walaClass.getClassHierarchy();
            }

            @Provides
            @Singleton
            public XMLMethodSummaryReader provideXMLMethodSummaries(final IClassHierarchy cha) {
                final ClassLoader cl = Util.class.getClassLoader();
                final InputStream s = cl.getResourceAsStream("natives.xml");
                final AnalysisScope scope = cha.getScope();
                final XMLMethodSummaryReader summary = new XMLMethodSummaryReader(s, scope);
                return summary;
            }

            @Provides
            @Singleton
            public AnalysisOptions provideOptions(final IClassHierarchy cha) {
                return new AnalysisOptions(cha.getScope(), null);
            }

            @Provides
            public IEntrypointSelector providesSelector() {
                return new AllMethodsAndContructorsEntrypointSelector();
            }
        });
    }
}
