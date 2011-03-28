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

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.CallGraphMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ConstructorSuperDeclarationMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredFieldsClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredInterfacesClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.DeclaredSuperclassClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.FingerprintCompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.FirstDeclarationMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICallGraphAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.IClassAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitFinalizer;
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
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.AllMethodsAndContructorsEntrypointSelector;
import org.eclipse.recommenders.internal.commons.analysis.entrypoints.IEntrypointSelector;
import org.eclipse.recommenders.internal.rcp.analysis.cp.BundleManifestSymbolicNameFinder;
import org.eclipse.recommenders.internal.rcp.analysis.cp.BundleManifestVersionFinder;
import org.eclipse.recommenders.internal.rcp.analysis.cp.FingerprintClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.IClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.INameFinder;
import org.eclipse.recommenders.internal.rcp.analysis.cp.IProjectClasspathAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.IVersionFinder;
import org.eclipse.recommenders.internal.rcp.analysis.cp.LocationClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.NameClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.ReportingProjectClasspathAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.TypesCollectorClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.rcp.analysis.cp.VersionClasspathEntryAnalyzer;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;

@SuppressWarnings("rawtypes")
public class RcpAnalysisModule extends AbstractModule implements com.google.inject.Module {
    public static final String CLASSPATH_ENTRY_STORE_BASEDIR = "classpathentry.store.basedir";

    @Override
    protected void configure() {
        bindClassHierarchyService();
        bindCompilationUnitAnalyzer();
        bindClasspathStore();
        bindClasspathAnalyzers();
        bindCompilationUnitAnalyzers();
    }

    private void bindCompilationUnitAnalyzers() {
        configureClassAnalyzer();
        configureMethodAnalyzer();
        configureCallgraphAnalyzer();
        configureCompilationUnitFinalizer();
        configureCompilationUnitConsumer();
        bind(AnalysisCache.class).toInstance(new AnalysisCache());
        bind(IEntrypointSelector.class).to(AllMethodsAndContructorsEntrypointSelector.class);
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
        b.addBinding().to(LocalNamesCollectingCallGraphAnalyzer.class);

    }

    private void configureCompilationUnitFinalizer() {
        final Multibinder<ICompilationUnitFinalizer> binder = Multibinder.newSetBinder(binder(),
                ICompilationUnitFinalizer.class);
        binder.addBinding().to(JavaLangInstanceKeysRemoverCompilationUnitFinalizer.class).in(Singleton.class);
        binder.addBinding().to(WalaDefaultInstanceKeysRemoverCompilationUnitFinalizer.class).in(Singleton.class);
        binder.addBinding().to(ThisObjectInstanceKeyCompilationUnitFinalizer.class).in(Singleton.class);
        binder.addBinding().to(FingerprintCompilationUnitFinalizer.class).in(Singleton.class);

    }

    private void bindClasspathStore() {
        storeClasspathStoreBasedir();
        bind(ClasspathEntryStore.class).asEagerSingleton();
    }

    private void storeClasspathStoreBasedir() {
        final RcpAnalysisPlugin plugin = RcpAnalysisPlugin.getDefault();
        final IPath stateLocation = plugin.getStateLocation();
        final IPath storeBasedirPath = stateLocation.append("/classpath-entries/");
        final File storeBaseDir = storeBasedirPath.toFile();
        storeBaseDir.mkdirs();
        bind(File.class).annotatedWith(Names.named(CLASSPATH_ENTRY_STORE_BASEDIR)).toInstance(storeBaseDir);

    }

    private void bindClassHierarchyService() {
        bind(IClassHierarchyService.class).to(WalaClassHierarchyService.class).in(Scopes.SINGLETON);
    }

    private void bindCompilationUnitAnalyzer() {
        final Multibinder<ICompilationUnitAnalyzer> binder = Multibinder.newSetBinder(binder(),
                ICompilationUnitAnalyzer.class);
        binder.addBinding().to(WalaCompilationUnitAnalyzerService.class).in(Singleton.class);
    }

    private void bindClasspathAnalyzers() {

        bind(IProjectClasspathAnalyzer.class).to(ReportingProjectClasspathAnalyzer.class).in(Scopes.SINGLETON);

        // classpath entry analyzer
        final Multibinder<IClasspathEntryAnalyzer> binder = Multibinder.newSetBinder(binder(),
                IClasspathEntryAnalyzer.class);
        binder.addBinding().to(LocationClasspathEntryAnalyzer.class).in(Singleton.class);
        binder.addBinding().to(NameClasspathEntryAnalyzer.class).in(Singleton.class);
        binder.addBinding().to(VersionClasspathEntryAnalyzer.class).in(Singleton.class);
        binder.addBinding().to(FingerprintClasspathEntryAnalyzer.class).in(Singleton.class);
        binder.addBinding().to(TypesCollectorClasspathEntryAnalyzer.class).in(Singleton.class);

        // version finder
        final Multibinder<IVersionFinder> versionFinderBinder = Multibinder
                .newSetBinder(binder(), IVersionFinder.class);
        versionFinderBinder.addBinding().to(BundleManifestVersionFinder.class).in(Scopes.SINGLETON);

        // name finder
        final Multibinder<INameFinder> nameFinderBinder = Multibinder.newSetBinder(binder(), INameFinder.class);
        nameFinderBinder.addBinding().to(BundleManifestSymbolicNameFinder.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public XMLMethodSummaryReader provideXMLMethodSummaries() {
        final AnalysisScope defaultScope = AnalysisScope.createJavaAnalysisScope();
        final ClassLoader cl = Util.class.getClassLoader();
        final InputStream s = cl.getResourceAsStream("natives.xml");
        final XMLMethodSummaryReader summary = new XMLMethodSummaryReader(s, defaultScope);
        return summary;
    }
}
