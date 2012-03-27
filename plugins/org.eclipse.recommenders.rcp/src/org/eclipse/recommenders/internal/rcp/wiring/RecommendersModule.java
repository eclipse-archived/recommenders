/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.wiring;

import static java.lang.Thread.MIN_PRIORITY;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.recommenders.internal.rcp.providers.CachingAstProvider;
import org.eclipse.recommenders.internal.rcp.providers.ClasspathEntryInfoProvider;
import org.eclipse.recommenders.internal.rcp.providers.JavaModelEventsProvider;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionProvider;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.eclipse.recommenders.rcp.repo.ModelRepositoryService;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils;
import org.eclipse.recommenders.utils.rcp.ast.ASTStringUtils;
import org.eclipse.recommenders.utils.rcp.ast.BindingUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;

@SuppressWarnings("restriction")
public class RecommendersModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        configureJavaElementResolver();
        configureAstProvider();
        bindRepository();
        initalizeSingletonServices();

    }

    @Singleton
    @Provides
    protected IClasspathEntryInfoProvider configurePackageFragmentRootInfoProvider(final EventBus bus,
            IWorkspaceRoot workspace) {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();
        IClasspathEntryInfoProvider cpeInfoProvider = new ClasspathEntryInfoProvider(stateLocation, workspace, bus);
        bus.register(cpeInfoProvider);
        return cpeInfoProvider;
    }

    private void configureJavaElementResolver() {
        bind(JavaElementResolver.class).in(Scopes.SINGLETON);
        requestStaticInjection(ASTStringUtils.class);
        requestStaticInjection(ASTNodeUtils.class);
        requestStaticInjection(BindingUtils.class);
    }

    private void configureAstProvider() {
        final CachingAstProvider p = new CachingAstProvider();
        JavaCore.addElementChangedListener(p);
        bind(IAstProvider.class).toInstance(p);
    }

    private void bindRepository() {
        bind(RepositoryUrlChangeListener.class).asEagerSingleton();

        Bundle bundle = FrameworkUtil.getBundle(getClass());
        File stateLocation = Platform.getStateLocation(bundle).toFile();

        File repo = new File(stateLocation, "repo");
        repo.mkdirs();
        String url = RecommendersPlugin.getDefault().getPreferenceStore()
                .getString(RecommendersPlugin.P_REPOSITORY_URL);
        bind(File.class).annotatedWith(LocalModelRepositoryLocation.class).toInstance(repo);
        bind(String.class).annotatedWith(RemoteModelRepositoryLocation.class).toInstance(url
        // "file:/Volumes/usb/juno/m2/"
        //
                );
        bind(IModelRepository.class).to(ModelRepository.class).in(Scopes.SINGLETON);

        File index = new File(stateLocation, "index");
        index.mkdirs();
        bind(File.class).annotatedWith(ModelRepositoryIndexLocation.class).toInstance(index);
        bind(IModelRepositoryIndex.class).to(ModelRepositoryIndex.class).in(Scopes.SINGLETON);
        bind(ModelRepositoryService.class).asEagerSingleton();
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface LocalModelRepositoryLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface RemoteModelRepositoryLocation {
    }

    @BindingAnnotation
    @Target(PARAMETER)
    @Retention(RUNTIME)
    public static @interface ModelRepositoryIndexLocation {
    }

    private void initalizeSingletonServices() {
        bind(ServicesInitializer.class).asEagerSingleton();
    }

    @Singleton
    @Provides
    protected JavaModelEventsProvider provideJavaModelEventsProvider(final EventBus bus, final IWorkspaceRoot workspace) {
        final JavaModelEventsProvider p = new JavaModelEventsProvider(bus, workspace);
        JavaCore.addElementChangedListener(p);
        return p;
    }

    @Singleton
    @Provides
    // @Workspace
    protected EventBus provideWorkspaceEventBus() {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService pool = coreThreadsTimoutExecutor(numberOfCores + 1, MIN_PRIORITY,
                "Recommenders-Bus-Thread-", 1L, TimeUnit.MINUTES);
        final EventBus bus = new AsyncEventBus("Code Recommenders asychronous Workspace Event Bus", pool);
        return bus;
    }

    @Provides
    @Singleton
    protected JavaSelectionProvider provideJavaSelectionProvider(final EventBus bus) {
        final JavaSelectionProvider provider = new JavaSelectionProvider(bus);
        new UIJob("Registering workbench selection listener.") {
            {
                schedule();
            }

            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                final IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                final ISelectionService service = (ISelectionService) ww.getService(ISelectionService.class);
                service.addPostSelectionListener(provider);
                return Status.OK_STATUS;
            }
        };
        return provider;
    }

    @Provides
    protected IWorkspaceRoot provideWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    @Provides
    protected IWorkspace provideWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    @Provides
    protected Display provideDisplay() {
        Display d = Display.getCurrent();
        if (d == null) {
            d = Display.getDefault();
        }
        return d;
    }

    @Provides
    protected IWorkbench provideWorkbench() {
        return PlatformUI.getWorkbench();
    }

    @Provides
    protected IWorkbenchPage provideActiveWorkbenchPage(final IWorkbench wb) {

        if (isRunningInUiThread()) {
            return wb.getActiveWorkbenchWindow().getActivePage();
        }

        return runUiFinder().activePage;
    }

    private ActivePageFinder runUiFinder() {
        final ActivePageFinder finder = new ActivePageFinder();
        try {
            if (isRunningInUiThread()) {
                finder.call();
            } else {
                final FutureTask<IWorkbenchPage> task = new FutureTask(finder);
                Display.getDefault().asyncExec(task);
                task.get(2, TimeUnit.SECONDS);
            }
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "Could not run 'active page finder' that early!");
        }
        return finder;
    }

    private boolean isRunningInUiThread() {
        return Display.getCurrent() != null;
    }

    @Provides
    protected IJavaModel provideJavaModel() {
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    @Provides
    protected JavaModelManager provideJavaModelManger() {
        return JavaModelManager.getJavaModelManager();
    }

    private final class ActivePageFinder implements Callable<IWorkbenchPage> {
        private IWorkbench workbench;
        private IWorkbenchWindow activeWorkbenchWindow;
        private IWorkbenchPage activePage;

        @Override
        public IWorkbenchPage call() throws Exception {
            workbench = PlatformUI.getWorkbench();
            activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            activePage = activeWorkbenchWindow.getActivePage();
            return activePage;
        }
    }

    /*
     * this is a bit odd. Used to initialize complex wired elements such as JavaElementsProvider etc.
     */
    public static class ServicesInitializer {

        public final IClasspathEntryInfoProvider pgkInfoProvider;

        @Inject
        private ServicesInitializer(IModelRepository repo, IModelRepositoryIndex index, final IAstProvider astProvider,
                final JavaModelEventsProvider eventsProvider, final JavaSelectionProvider selectionProvider,
                IClasspathEntryInfoProvider pgkInfoProvider) {
            this.pgkInfoProvider = pgkInfoProvider;
            index.open();
        }
    }

}
