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

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfigurations;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;

public class SnipmatchRcpModule extends AbstractModule {

    public static final String SNIPPET_REPOSITORY_BASEDIR = "SNIPPET_REPOSITORY_BASEDIR"; //$NON-NLS-1$
    public static final String SNIPPET_REPOSITORY_PROVIDERS = "SNIPPET_REPOSITORY_PROVIDERS"; //$NON-NLS-1$
    public static final String REPOSITORY_CONFIGURATION_FILE = "REPOSITORY_CONFIGURATION_FILE"; //$NON-NLS-1$
    private static final String SNIPMATCH_ROOT_FOLDER = "SNIPMATCH_ROOT_FOLDER"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(SnipmatchRcpModule.class);

    @Override
    protected void configure() {
        bind(Repositories.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @Named(SNIPMATCH_ROOT_FOLDER)
    public File provideSnipmatchRoot(IWorkspaceRoot root) {
        File recommendersRoot = new File(root.getLocation().toFile(), ".recommenders"); //$NON-NLS-1$
        File snipmatchRoot = new File(recommendersRoot, "snipmatch"); //$NON-NLS-1$
        return snipmatchRoot;
    }

    @Provides
    @Singleton
    @Named(SNIPPET_REPOSITORY_BASEDIR)
    public File provideBasedir(IWorkspaceRoot root, @Named(SNIPMATCH_ROOT_FOLDER) File snipmatchRoot) {
        File snippetRepositoryBasedir = new File(snipmatchRoot, "repositories"); //$NON-NLS-1$
        try {
            Files.createParentDirs(snippetRepositoryBasedir);
        } catch (IOException e) {
            LOG.error("Failed to bind file name {}.", snippetRepositoryBasedir, e); //$NON-NLS-1$
        }
        return snippetRepositoryBasedir;
    }

    @Provides
    @Singleton
    @Named(REPOSITORY_CONFIGURATION_FILE)
    public File provideConfigurationFile(IWorkspaceRoot root, @Named(SNIPMATCH_ROOT_FOLDER) File snipmatchRoot) {
        return new File(snipmatchRoot, "repositoryconfiguration.config");
    }

    @Provides
    @Singleton
    public SnippetRepositoryConfigurations provideRepositoryConfigurations() {
        SnippetRepositoryConfigurations configurations = RepositoryConfigurations.loadConfigurations();
        if (configurations.getRepos().isEmpty()) {
            configurations.getRepos().addAll(RepositoryConfigurations.fetchDefaultConfigurations());
        }
        return configurations;
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
