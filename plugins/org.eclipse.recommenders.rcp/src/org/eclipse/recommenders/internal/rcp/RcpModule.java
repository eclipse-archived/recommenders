/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static com.google.inject.Scopes.SINGLETON;
import static java.lang.Thread.MIN_PRIORITY;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.eclipse.recommenders.internal.rcp.LogMessages.*;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.ASTNodeUtils;
import org.eclipse.recommenders.rcp.utils.ASTStringUtils;
import org.eclipse.recommenders.rcp.utils.AstBindings;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

@SuppressWarnings("restriction")
public class RcpModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        bind(JavaElementResolver.class).in(SINGLETON);
        requestStaticInjection(ASTStringUtils.class);
        requestStaticInjection(ASTNodeUtils.class);
        requestStaticInjection(AstBindings.class);
        bind(Helper.class).asEagerSingleton();
        bind(SharedImages.class).in(SINGLETON);
        configureAstProvider();
        bindRcpServiceListener();
        checkBundleResolution();
    }

    private void configureAstProvider() {
        final CachingAstProvider p = new CachingAstProvider();
        JavaCore.addElementChangedListener(p);
        bind(IAstProvider.class).toInstance(p);
    }

    private void bindRcpServiceListener() {
        bindListener(new RcpServiceMatcher(), new Listener());
    }

    @Singleton
    @Provides
    public JavaModelEventsService provideJavaModelEventsProvider(final EventBus bus, final IWorkspaceRoot workspace) {
        final JavaModelEventsService p = new JavaModelEventsService(bus, workspace);
        JavaCore.addElementChangedListener(p);
        return p;
    }

    @Singleton
    @Provides
    public EventBus provideWorkspaceEventBus() {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService pool = coreThreadsTimoutExecutor(numberOfCores + 1, MIN_PRIORITY,
                "Recommenders-Bus-Thread-", //$NON-NLS-1$
                1L, TimeUnit.MINUTES);
        return new AsyncEventBus("Recommenders asychronous Workspace Event Bus", pool); //$NON-NLS-1$
    }

    @Provides
    public IWebBrowser provideWebBrowser(IWorkbench wb) throws PartInitException {
        return wb.getBrowserSupport().getExternalBrowser();
    }

    @Provides
    @Singleton
    public JavaElementSelectionService provideJavaSelectionProvider(final EventBus bus) {
        final JavaElementSelectionService provider = new JavaElementSelectionService(bus);
        new UIJob("Registering workbench selection listener.") { //$NON-NLS-1$
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
    public IWorkspaceRoot provideWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    @Provides
    public IWorkspace provideWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    @Provides
    public Display provideDisplay() {
        Display d = Display.getCurrent();
        if (d == null) {
            d = Display.getDefault();
        }
        return d;
    }

    @Provides
    public IWorkbench provideWorkbench() {
        return PlatformUI.getWorkbench();
    }

    @Provides
    public IWorkbenchPage provideActiveWorkbenchPage(final IWorkbench wb) {

        if (isRunningInUiThread()) {
            return wb.getActiveWorkbenchWindow().getActivePage();
        }

        return runUiFinder().activePage;
    }

    @Provides
    public Shell provideActiveShell(IWorkbench wb) {
        return wb.getActiveWorkbenchWindow().getShell();
    }

    private ActivePageFinder runUiFinder() {
        final ActivePageFinder finder = new ActivePageFinder();
        try {
            if (isRunningInUiThread()) {
                finder.call();
            } else {
                final FutureTask<IWorkbenchPage> task = new FutureTask<IWorkbenchPage>(finder);
                Display.getDefault().asyncExec(task);
                task.get(2, TimeUnit.SECONDS);
            }
        } catch (final Exception e) {
            log(ACTIVE_PAGE_FINDER_TOO_EARLY, e);
        }
        return finder;
    }

    private boolean isRunningInUiThread() {
        return Display.getCurrent() != null;
    }

    @Provides
    public IJavaModel provideJavaModel() {
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    @Provides
    public JavaModelManager provideJavaModelManger() {
        return JavaModelManager.getJavaModelManager();
    }

    @Provides
    public IExtensionRegistry provideRegistry() {
        return Platform.getExtensionRegistry();
    }

    private void checkBundleResolution() {
        Bundle[] bundles = RcpPlugin.getDefault().getBundle().getBundleContext().getBundles();

        final Collection<Bundle> unresolvedBundles = Lists.newArrayList();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().startsWith("org.eclipse.recommenders")) { //$NON-NLS-1$
                if (bundle.getState() == Bundle.INSTALLED) {
                    unresolvedBundles.add(bundle);
                }
            }
        }
        if (!unresolvedBundles.isEmpty()) {
            final Display display = Display.getDefault();
            display.asyncExec(new Runnable() {

                @Override
                public void run() {

                    BundleResolutionFailureDialog dialog = new BundleResolutionFailureDialog(display.getActiveShell(),
                            RcpPlugin.getDefault().getBundle().getVersion(), unresolvedBundles);
                    if (!dialog.isIgnored()) {
                        dialog.open();
                    }
                }
            });
        }
    }

    static class RcpServiceMatcher extends AbstractMatcher<Object> {

        @Override
        public boolean matches(Object t) {
            if (t instanceof TypeLiteral<?>) {
                Class<?> rawType = ((TypeLiteral<?>) t).getRawType();
                Class<?>[] implemented = rawType.getInterfaces();
                return contains(implemented, IRcpService.class);
            }
            return false;
        }
    }

    /**
     * Ensures that the services are created
     */
    static class Helper {

        @Inject
        JavaElementSelectionService provider;

        @Inject
        JavaModelEventsService JavaModelEventsService;
    }

    static class Listener implements TypeListener {

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            final Provider<EventBus> provider = encounter.getProvider(EventBus.class);

            encounter.register(new InjectionListener<I>() {

                @Override
                public void afterInjection(final Object i) {
                    registerWithEventBus(i);
                    for (final Method m : i.getClass().getDeclaredMethods()) {
                        boolean hasPostConstruct = m.getAnnotation(PostConstruct.class) != null;
                        boolean hasPreDestroy = m.getAnnotation(PreDestroy.class) != null;
                        if (hasPreDestroy) {
                            registerPreDestroyHook(i, m);
                        }
                        if (hasPostConstruct) {
                            executeMethod(i, m);
                        }
                    }
                }

                private void executeMethod(final Object i, final Method m) {
                    try {
                        m.setAccessible(true);
                        m.invoke(i);
                    } catch (Exception e) {
                        Logs.log(EXCEPTION_OCCURRED_IN_SERVICE_HOOK, e, m);
                    }
                }

                private void registerPreDestroyHook(final Object i, final Method m) {
                    PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

                        @Override
                        public boolean preShutdown(IWorkbench workbench, boolean forced) {
                            return true;
                        }

                        @Override
                        public void postShutdown(IWorkbench workbench) {
                            executeMethod(i, m);
                        }
                    });
                }

                private void registerWithEventBus(final Object i) {
                    EventBus bus = provider.get();
                    bus.register(i);
                }
            });
        }
    }

    private static final class ActivePageFinder implements Callable<IWorkbenchPage> {

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
}
