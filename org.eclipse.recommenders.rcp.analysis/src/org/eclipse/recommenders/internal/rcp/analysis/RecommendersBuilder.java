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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.annotations.Testing;
import org.eclipse.recommenders.internal.rcp.InterruptingProgressMonitor;
import org.eclipse.recommenders.internal.rcp.analysis.cp.IProjectClasspathAnalyzer;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;
import org.eclipse.recommenders.rcp.utils.CountingProgressMonitor;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * Incremental builder that triggers the {@link ICompilationUnitAnalyzer}s to
 * analyze changed java resources whenever required.
 */
@SuppressWarnings("rawtypes")
public class RecommendersBuilder extends IncrementalProjectBuilder {
    private static int ticksLastFullBuild = 100;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static int ticksLastIncrBuild = 2;

    private CountingProgressMonitor monitor;

    @Inject
    private IArtifactStore store;

    @Inject
    private Set<ICompilationUnitAnalyzer> analyzers;

    @Inject
    private IProjectClasspathAnalyzer cpAnalyzer;
    @Inject
    private IClassHierarchyService chaService;

    @Inject
    private JavaElementResolver javaElementResolver;

    public RecommendersBuilder() {
        // that's odd: builder extension point does not allow usage of extension
        // factories. Thus, we have to set up things manually.
        final InjectionService service = InjectionService.getInstance();
        service.injectMembers(this);
    }

    @Testing
    protected RecommendersBuilder(final IArtifactStore store, final Set<ICompilationUnitAnalyzer> analyzers,
            final IProjectClasspathAnalyzer cpAnalyzer) {
        this.store = store;
        this.analyzers = analyzers;
    }

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        setMonitor(monitor);
        performCleanBuild();
    }

    @Override
    protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor) throws CoreException {
        setMonitor(monitor);
        switch (kind) {
        case FULL_BUILD:
            performFullBuild();
            return null;
        case CLEAN_BUILD:
            performCleanBuild();
            return null;
        case INCREMENTAL_BUILD:
        case AUTO_BUILD:
        default:
            final IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                performFullBuild();
            } else {
                performIncrementalBuild(delta);
            }
            return null;
        }
    }

    private void setMonitor(final IProgressMonitor monitor) {
        this.monitor = new CountingProgressMonitor(monitor);
    }

    private void performCleanBuild() throws CoreException {
        monitor.subTask("Cleaning " + getProject().getName());
        store.cleanStore(getProject());
        monitor.worked(1);
    }

    private void performFullBuild() throws CoreException {
        monitor.beginTask("Perform Full Build", ticksLastFullBuild);
        performCleanBuild();
        getProject().accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
                final IJavaElement element = JavaCore.create(resource);
                if (element instanceof ICompilationUnit) {
                    analyzeCompilationUnit((ICompilationUnit) element);
                }
                return element != null;
            }
        });

        analyzeClasspath();

        ticksLastFullBuild = monitor.actualWork;
    }

    private void analyzeClasspath() {
        // final IJavaProject javaProject = JavaCore.create(getProject());
        // final SubProgressMonitor submonitor = new SubProgressMonitor(monitor,
        // 5);
        // final ProjectClasspath cp = cpAnalyzer.analyze(javaProject,
        // submonitor);
        // store.storeArtifact(javaProject, cp);
    }

    private void performIncrementalBuild(final IResourceDelta delta) throws CoreException {
        monitor.beginTask("Incremental Build", ticksLastIncrBuild);
        delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(final IResourceDelta delta) throws CoreException {
                final IResource resource = delta.getResource();
                final IJavaElement element = JavaCore.create(resource);
                if (element instanceof ICompilationUnit) {
                    final ICompilationUnit cu = (ICompilationUnit) element;
                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.CHANGED:
                    case IResourceDelta.REPLACED:
                        analyzeCompilationUnit(cu);
                        break;
                    case IResourceDelta.REMOVED:
                        removeCompilationUnit(cu);
                        break;
                    default:
                        break;
                    }
                }
                return element != null;
            }
        });
        ticksLastIncrBuild = monitor.actualWork;
    }

    private void analyzeCompilationUnit(final ICompilationUnit cu) throws CoreException {
        if (monitor.isCanceled()) {
            return;
        }

        final Future<?> f = executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    final IClassHierarchy cha = chaService.getClassHierachy(cu);
                    if (cha instanceof LazyClassHierarchy) {
                        final LazyClassHierarchy lcha = (LazyClassHierarchy) cha;
                        final IType primaryType = cu.findPrimaryType();
                        if (primaryType != null) {
                            lcha.remove(javaElementResolver.toRecType(primaryType));
                        }
                    }
                    if (!cu.isStructureKnown()) {
                        monitor.subTask("Skipping " + cu.getElementName() + " because of syntax errors.");
                        return;
                    }
                    monitor.subTask("Recommenders Analyzing " + cu.getElementName());
                    final List<Object> artifacts = Lists.newLinkedList();
                    for (final ICompilationUnitAnalyzer<?> analyzer : analyzers) {

                        final Object artifact = safeAnalyzeCompilationUnit(cu, analyzer,
                                new InterruptingProgressMonitor(monitor));
                        if (artifact != null) {
                            artifacts.add(artifact);
                        }
                    }
                    store.storeArtifacts(cu, artifacts);
                    monitor.worked(1);

                } catch (final CoreException e) {
                    e.printStackTrace();
                }
            }
        });
        // try {
        // // f.get(1000, TimeUnit.MILLISECONDS);
        // } catch (final InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (final ExecutionException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (final TimeoutException e) {
        // f.cancel(true);
        // }

    }

    private Object safeAnalyzeCompilationUnit(final ICompilationUnit cu, final ICompilationUnitAnalyzer<?> analyzer,
            final IProgressMonitor monitor) {

        try {
            return analyzer.analyze(cu, monitor);
        } catch (final RuntimeException x) {
            logAnalyzerFailed(cu, analyzer, x);
            return null;
        }
    }

    private void logAnalyzerFailed(final ICompilationUnit cu, final ICompilationUnitAnalyzer<?> analyzer,
            final Exception x) {
        final String analyzerName = analyzer.getClass().getSimpleName();
        final String cuName = cu.getElementName();
        RecommendersPlugin.logError(x, "Analyzer '%s' threw exception while analzying '%s'", analyzerName, cuName);
    }

    private void removeCompilationUnit(final ICompilationUnit cu) throws CoreException {
        monitor.subTask("Removing Analysis Artifacts for " + cu.getElementName());
        store.removeArtifacts(cu);
        monitor.worked(1);
    }
}
