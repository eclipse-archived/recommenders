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
package org.eclipse.recommenders.internal.analysis.rcp;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.recommenders.internal.analysis.rcp.cp.BundleManifestSymbolicNameFinder;
import org.eclipse.recommenders.internal.analysis.rcp.cp.BundleManifestVersionFinder;
import org.eclipse.recommenders.internal.analysis.rcp.cp.FingerprintClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.IClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.INameFinder;
import org.eclipse.recommenders.internal.analysis.rcp.cp.IProjectClasspathAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.IVersionFinder;
import org.eclipse.recommenders.internal.analysis.rcp.cp.LocationClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.NameClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.ReportingProjectClasspathAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.TypesCollectorClasspathEntryAnalyzer;
import org.eclipse.recommenders.internal.analysis.rcp.cp.VersionClasspathEntryAnalyzer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

@SuppressWarnings("rawtypes")
public class RcpAnalysisModule extends AbstractModule implements com.google.inject.Module {
    public static final String CLASSPATH_ENTRY_STORE_BASEDIR = "classpathentry.store.basedir";

    @Override
    protected void configure() {
        // bindClassHierarchyService();
        // bindCompilationUnitAnalyzer();
        bindClasspathStore();
        bindClasspathAnalyzers();
        // bindCompilationUnitAnalyzers();
        bindProjectLifeCycleService();
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

    private void bindProjectLifeCycleService() {
        bind(RecommendersProjectLifeCycleService.class).asEagerSingleton();
        Multibinder.newSetBinder(binder(), IRecommendersProjectLifeCycleListener.class);
    }

}
