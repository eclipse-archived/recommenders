/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.coordinates.rcp;

import static com.google.inject.Scopes.SINGLETON;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.IProjectCoordinateAdvisorService;
import org.eclipse.recommenders.coordinates.rcp.EclipseProjectCoordinateAdvisorService;
import org.eclipse.recommenders.internal.coordinates.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.ui.IWorkbench;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class CoordinatesRcpModule extends AbstractModule {

    public static final String IDENTIFIED_PROJECT_COORDINATES = "IDENTIFIED_PACKAGE_FRAGMENT_ROOTS"; //$NON-NLS-1$
    public static final String MANUAL_MAPPINGS = "MANUAL_MAPPINGS"; //$NON-NLS-1$

    @Override
    protected void configure() {
        bind(EclipseProjectCoordinateAdvisorService.class).in(SINGLETON);
        bind(IProjectCoordinateAdvisorService.class).to(EclipseProjectCoordinateAdvisorService.class);

        // configure caching
        bind(ManualProjectCoordinateAdvisor.class).in(SINGLETON);
        createAndBindPerWorkspaceNamedFile("caches/manual-mappings.json", MANUAL_MAPPINGS); //$NON-NLS-1$
        createAndBindPerWorkspaceNamedFile("caches/identified-project-coordinates.json", //$NON-NLS-1$
                IDENTIFIED_PROJECT_COORDINATES);
    }

    private void createAndBindPerWorkspaceNamedFile(String fileName, String name) {
        File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        File dotRecommenders = new File(workspaceRoot, ".recommenders"); //$NON-NLS-1$
        createAndBindNamedFile(fileName, name, dotRecommenders);
    }

    private void createAndBindNamedFile(String fileName, String name, File stateLocation) {
        File file = new File(stateLocation, fileName);
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_BIND_FILE_NAME, e, fileName);
        }
        bind(File.class).annotatedWith(Names.named(name)).toInstance(file);
    }

    @Singleton
    @Provides
    public IDependencyListener provideDependencyListener(EventBus bus) {
        return new EclipseDependencyListener(bus);
    }

    @Provides
    @Singleton
    public CoordinatesRcpPreferences provide(IWorkbench wb, EventBus bus) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        context.set(EventBus.class, bus);
        return ContextInjectionFactory.make(CoordinatesRcpPreferences.class, context);
    }
}
