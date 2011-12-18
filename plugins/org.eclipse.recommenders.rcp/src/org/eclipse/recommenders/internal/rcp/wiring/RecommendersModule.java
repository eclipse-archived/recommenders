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
package org.eclipse.recommenders.internal.rcp.wiring;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.recommenders.internal.rcp.providers.CachingAstProvider;
import org.eclipse.recommenders.internal.rcp.providers.JavaModelEventsProvider;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionProvider;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils;
import org.eclipse.recommenders.utils.rcp.ast.ASTStringUtils;
import org.eclipse.recommenders.utils.rcp.ast.BindingUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
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
        initalizeSingletonServices();
    }

    private void initalizeSingletonServices() {
        bind(ServicesInitializer.class).asEagerSingleton();
    }

    @Singleton
    @Provides
    public JavaModelEventsProvider provideJavaElementEventsProvider(final EventBus bus) {
        final JavaModelEventsProvider p = new JavaModelEventsProvider(bus);
        JavaCore.addElementChangedListener(p);
        return p;
    }

    @Singleton
    @Provides
    public EventBus provideWorkspaceEventBus() {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, numberOfCores, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        final EventBus bus = new AsyncEventBus("Code Recommenders asychronous Workspace Event Bus", pool);
        return bus;
    }

    private void configureAstProvider() {
        final CachingAstProvider p = new CachingAstProvider();
        JavaCore.addElementChangedListener(p);
        bind(IAstProvider.class).toInstance(p);
    }

    @Provides
    @Singleton
    public static JavaSelectionProvider provideSelectionListener(final EventBus bus, final IWorkbench wb) {
        final JavaSelectionProvider provider = new JavaSelectionProvider(bus);
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                final IWorkbenchWindow ww = wb.getActiveWorkbenchWindow();
                final ISelectionService service = (ISelectionService) ww.getService(ISelectionService.class);
                service.addPostSelectionListener(provider);
            }
        });
        return provider;
    }

    private void configureJavaElementResolver() {
        bind(JavaElementResolver.class).in(Scopes.SINGLETON);
        requestStaticInjection(ASTStringUtils.class);
        requestStaticInjection(ASTNodeUtils.class);
        requestStaticInjection(BindingUtils.class);
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
    public IWorkbench provideWorkbench() {
        return PlatformUI.getWorkbench();
    }

    @Provides
    public IJavaModel provideJavaModel() {
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    @Provides
    public JavaModelManager provideJavaModelManger() {
        return JavaModelManager.getJavaModelManager();
    }

    /*
     * this is a bit odd. Used to initialize complex wired elements such as JavaElementsProvider etc.
     */
    public static class ServicesInitializer {

        @Inject
        private ServicesInitializer(final IAstProvider astProvider, final JavaModelEventsProvider eventsProvider,
                final JavaSelectionProvider selectionProvider) {
            ensureIsNotNull(astProvider);
            ensureIsNotNull(eventsProvider);
        }
    }

}
