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

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.util.debug.UnimplementedError;

public class WalaCompilationUnitAnalyzerService implements ICompilationUnitAnalyzer<CompilationUnit> {
    private final IClassHierarchyService wala;

    private Injector injector;

    @Inject
    public WalaCompilationUnitAnalyzerService(final IClassHierarchyService wala) {
        this.wala = wala;
    }

    @Override
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
        final StopWatch w = new StopWatch();
        w.start();

        final Injector masterInjector = InjectionService.getInstance().getInjector();
        injector = masterInjector;
        // .createChildInjector(new AbstractModule() {
        // @Override
        // protected void configure() {
        // configureClassAnalyzer();
        // configureMethodAnalyzer();
        // configureCallgraphAnalyzer();
        // configureCompilationUnitFinalizer();
        // configureCompilationUnitConsumer();
        // // bind(AnalysisCache.class).toInstance(new AnalysisCache());
        // bind(WalaTypeAnalyzer.class);
        // bind(WalaMethodAnalyzer.class);
        // bind(WalaCompiliationUnitAnalzyer.class);
        // }
        //
        // private void configureCompilationUnitConsumer() {
        // // empty binder
        // Multibinder.newSetBinder(binder(), ICompilationUnitConsumer.class);
        // }
        //
        // private void configureClassAnalyzer() {
        // final Multibinder<IClassAnalyzer> b =
        // Multibinder.newSetBinder(binder(), IClassAnalyzer.class);
        // b.addBinding().to(NameClassAnalyzer.class);
        // b.addBinding().to(ModifiersClassAnalyzer.class);
        // b.addBinding().to(DeclaredSuperclassClassAnalyzer.class);
        // b.addBinding().to(DeclaredInterfacesClassAnalyzer.class);
        // b.addBinding().to(DeclaredFieldsClassAnalyzer.class);
        //
        // }
        //
        // private void configureMethodAnalyzer() {
        // final Multibinder<IMethodAnalyzer> b =
        // Multibinder.newSetBinder(binder(), IMethodAnalyzer.class);
        // b.addBinding().to(ModifiersMethodAnalyzer.class);
        // b.addBinding().to(NameMethodAnalyzer.class);
        // b.addBinding().to(LineNumberMethodAnalyzer.class);
        // b.addBinding().to(FirstDeclarationMethodAnalyzer.class);
        // b.addBinding().to(SuperDeclarationMethodAnalyzer.class);
        // b.addBinding().to(ConstructorSuperDeclarationMethodAnalyzer.class);
        // b.addBinding().to(CallGraphMethodAnalyzer.class);
        // }
        //
        // private void configureCallgraphAnalyzer() {
        // final Multibinder<ICallGraphAnalyzer> b =
        // Multibinder.newSetBinder(binder(), ICallGraphAnalyzer.class);
        // b.addBinding().to(ParameterCallsitesCallGraphAnalyzer.class);
        // b.addBinding().to(ReceiverCallsitesCallGraphAnalyzer.class);
        // b.addBinding().to(ReceiverCallsitesCallGraphAnalyzer.class);
        // b.addBinding().to(LocalNamesCollectingCallGraphAnalyzer.class);
        //
        // }
        //
        // private void configureCompilationUnitFinalizer() {
        // final Multibinder<ICompilationUnitFinalizer> binder =
        // Multibinder.newSetBinder(binder(),
        // ICompilationUnitFinalizer.class);
        // binder.addBinding().to(JavaLangInstanceKeysRemoverCompilationUnitFinalizer.class).in(Singleton.class);
        // binder.addBinding().to(WalaDefaultInstanceKeysRemoverCompilationUnitFinalizer.class)
        // .in(Singleton.class);
        // binder.addBinding().to(ThisObjectInstanceKeyCompilationUnitFinalizer.class).in(Singleton.class);
        // binder.addBinding().to(FingerprintCompilationUnitFinalizer.class).in(Singleton.class);
        //
        // }
        //
        // @Provides
        // @Singleton
        // public IClassHierarchy provideClassHierarchy() {
        // return walaClass.getClassHierarchy();
        // }
        //
        // // @Provides
        // // @Singleton
        // // public AnalysisOptions provideOptions(final IClassHierarchy cha)
        // // {
        // // return new AnalysisOptions(cha.getScope(), null);
        // // }
        //
        // @Provides
        // public IEntrypointSelector providesSelector() {
        // return new AllMethodsAndContructorsEntrypointSelector();
        // }
        // });
        w.stop();
        System.out.println(w);
    }
}
