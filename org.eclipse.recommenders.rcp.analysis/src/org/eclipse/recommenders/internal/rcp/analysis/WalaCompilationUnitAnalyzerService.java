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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.CallGraphMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ConstructorSuperDeclarationMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.DeclaredFieldsClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ExtendsClauseClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.FingerprintCompilationUnitFinalizerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.FirstDeclarationMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ImplementsClauseClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.JavaLangInstanceKeysCompilationUnitFinalizerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.LineNumberMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.LocalNamesCollectingGraphAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ModifiersClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ModifiersMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.NameClassAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.NameMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ParameterCallsitesCallGraphAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ReceiverCallsitesCallGraphAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.SuperDeclarationMethodAnalyzerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ThisObjectInstanceKeyCompilationUnitFinalizerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.WalaDefaultInstanceKeysCompilationUnitFinalizerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.AllMethodsAndContructorsEntrypointSelector;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.IEntrypointSelector;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.cha.IClassHierarchy;
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
        final Injector masterInjector = InjectionService.getInstance().getInjector();
        injector = masterInjector.createChildInjector(new NameClassAnalyzerPluginModule(),
                new ExtendsClauseClassAnalyzerPluginModule(), new ImplementsClauseClassAnalyzerPluginModule(),
                new DeclaredFieldsClassAnalyzerPluginModule(), new NameMethodAnalyzerPluginModule(),
                new LineNumberMethodAnalyzerPluginModule(), new FirstDeclarationMethodAnalyzerPluginModule(),
                new SuperDeclarationMethodAnalyzerPluginModule(),
                new ConstructorSuperDeclarationMethodAnalyzerPluginModule(), new CallGraphMethodAnalyzerPluginModule(),
                new ParameterCallsitesCallGraphAnalyzerPluginModule(),
                new ReceiverCallsitesCallGraphAnalyzerPluginModule(),
                new LocalNamesCollectingGraphAnalyzerPluginModule(),
                new JavaLangInstanceKeysCompilationUnitFinalizerPluginModule(),
                new WalaDefaultInstanceKeysCompilationUnitFinalizerPluginModule(),
                new ModifiersMethodAnalyzerPluginModule(), new ModifiersClassAnalyzerPluginModule(),
                new ThisObjectInstanceKeyCompilationUnitFinalizerPluginModule(),
                new FingerprintCompilationUnitFinalizerPluginModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        final Multibinder<ICompilationUnitConsumer> binder = Multibinder.newSetBinder(binder(),
                                ICompilationUnitConsumer.class);
                    }

                    @Provides
                    public IClassHierarchy provideClassHierarchy() {
                        return walaClass.getClassHierarchy();
                    }

                    @Provides
                    public AnalysisOptions provideOptions(final IClassHierarchy cha) {
                        return new AnalysisOptions(cha.getScope(), null);
                    }

                    @Provides
                    public IEntrypointSelector providesSelector() {
                        return new AllMethodsAndContructorsEntrypointSelector();
                    }
                });
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
}
