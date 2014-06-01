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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchRcpPreferences.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;

public class SnipmatchRcpModule extends AbstractModule {

    public static final String SNIPPET_REPOSITORY_BASEDIR = "SNIPPET_REPOSITORY_BASEDIR"; //$NON-NLS-1$
    public static final String SNIPPET_REPOSITORY_PROVIDERS = "SNIPPET_REPOSITORY_PROVIDERS"; //$NON-NLS-1$

    private static final String EXT_ID_PROVIDER = "org.eclipse.recommenders.snipmatch.rcp.providers"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(SnipmatchRcpModule.class);

    @Override
    protected void configure() {
        bind(Repositories.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public SnipmatchRcpPreferences provide(IWorkbench wb, EventBus bus,
            @Named(SNIPPET_REPOSITORY_PROVIDERS) ImmutableSet<ISnippetRepositoryProvider> providers) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        SnipmatchRcpPreferences prefs = new SnipmatchRcpPreferences(bus, providers);
        ContextInjectionFactory.inject(prefs, context);
        return prefs;
    }

    @Provides
    @Singleton
    @Named(SNIPPET_REPOSITORY_PROVIDERS)
    public ImmutableSet<ISnippetRepositoryProvider> provide(EventBus bus) {
        List<ISnippetRepositoryProvider> registeredProviders = getRegisteredProviders();
        updateDefaultPreferences(registeredProviders);
        updatePreferences(registeredProviders);
        return ImmutableSet.copyOf(registeredProviders);
    }

    private static List<ISnippetRepositoryProvider> getRegisteredProviders() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_ID_PROVIDER);

        final List<ISnippetRepositoryProvider> providers = Lists.newArrayList();
        for (final IConfigurationElement element : elements) {
            try {
                providers.add((ISnippetRepositoryProvider) element.createExecutableExtension("class")); //$NON-NLS-1$
            } catch (CoreException e) {
                LOG.error("Exception while creating provider.", e); //$NON-NLS-1$
            }
        }
        return providers;
    }

    @Provides
    @Singleton
    @Named(SNIPPET_REPOSITORY_BASEDIR)
    public File provideBasedir(IWorkspaceRoot root) {
        File recommendersRoot = new File(root.getLocation().toFile(), ".recommenders"); //$NON-NLS-1$
        File snipmatchRoot = new File(recommendersRoot, "snipmatch"); //$NON-NLS-1$
        File snippetRepositoryBasedir = new File(snipmatchRoot, "repositories"); //$NON-NLS-1$
        try {
            Files.createParentDirs(snippetRepositoryBasedir);
        } catch (IOException e) {
            LOG.error("Failed to bind file name {}.", snippetRepositoryBasedir, e); //$NON-NLS-1$
        }
        return snippetRepositoryBasedir;
    }

    @Provides
    public IThemeManager provideThemeManager(IWorkbench wb) throws PartInitException {
        return wb.getThemeManager();
    }

    @Provides
    public ITheme provideThemeManager(IThemeManager mgr) throws PartInitException {
        return mgr.getCurrentTheme();
    }

    @Provides
    public ColorRegistry provideColorRegistry(ITheme theme) throws PartInitException {
        return theme.getColorRegistry();
    }

    @Provides
    public FontRegistry provideFontRegistry(ITheme theme) throws PartInitException {
        return theme.getFontRegistry();
    }
}
