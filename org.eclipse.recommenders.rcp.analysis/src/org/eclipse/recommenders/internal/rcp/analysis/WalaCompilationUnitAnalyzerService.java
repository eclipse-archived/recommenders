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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.debug.UnimplementedError;

public class WalaCompilationUnitAnalyzerService implements ICompilationUnitAnalyzer<CompilationUnit> {
    private final IClassHierarchyService wala;

    private Injector injector;

    @Inject
    public WalaCompilationUnitAnalyzerService(final IClassHierarchyService wala) {
        this.wala = wala;
    }

    private final SimpleTimeLimiter limiter = new SimpleTimeLimiter();

    @Override
    public Option<CompilationUnit> analyze(final ICompilationUnit jdtCompilationUnit, final IProgressMonitor monitor) {
        final StopWatch w = new StopWatch();
        w.start();
        CompilationUnit res = null;
        try {
            res = limiter.callWithTimeout(new Callable<CompilationUnit>() {

                @Override
                public CompilationUnit call() throws Exception {
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
                    monitor.beginTask("analyzing " + jdtCompilationUnit.getElementName(), 10);
                    r.run(monitor);
                    return recCompilationUnit;
                }

            }, 5, TimeUnit.SECONDS, true);

        } catch (final CancellationException x) {
            RcpAnalysisPlugin.logWarning(x,
                    "Analysis of '%s' exceeded max compuation time limit, and thus, has been canceled.",
                    jdtCompilationUnit.getElementName());
        } catch (final IllegalStateException x) {
            final Throwable rootCause = Throwables.getRootCause(x);
            if (rootCause instanceof CancelException || rootCause instanceof CallGraphBuilderCancelException) {
                RcpAnalysisPlugin.logWarning(x,
                        "Analysis of '%s' exceeded max compuation time limit, and thus, has been canceled.",
                        jdtCompilationUnit.getElementName());
            } else {
                RcpAnalysisPlugin.logError(x, "Analysis of %s failed with excpetion: %s",
                        jdtCompilationUnit.getElementName(), x.getMessage());
            }
        } catch (final UncheckedTimeoutException x) {
            RcpAnalysisPlugin.logWarning(x,
                    "Analysis of '%s' exceeded max compuation time limit, and thus, has been canceled.",
                    jdtCompilationUnit.getElementName());

        } catch (final Exception x) {
            RcpAnalysisPlugin.logError(x, "Error during analysis of '%s' : %s", jdtCompilationUnit.getElementName(),
                    x.getMessage());
        } catch (final UnimplementedError x) {
            RcpAnalysisPlugin.logError(x, "error during analysis of '%s'", jdtCompilationUnit.getElementName());
        } finally {
            monitor.done();
        }
        return Option.wrap(res);

    }

    private void createAnalysisInjector(final IClass walaClass) {
        // final StopWatch w = new StopWatch();
        // w.start();

        final Injector masterInjector = InjectionService.getInstance().getInjector();
        injector = masterInjector;
        // .createChildInjector(new AbstractModule() {
        // @Override
        // protected void configure() {
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
        // w.stop();
        // System.out.println(w);
    }
}
