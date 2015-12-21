/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch, Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.rcp.DependencyInfos;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;

public class TextContentAssistProcessor extends AbstractContentAssistProcessor<TextContentAssistInvocationContext> {

    @Inject
    public TextContentAssistProcessor(SnippetRepositoryConfigurations configs, Repositories repos,
            IProjectCoordinateProvider pcProvider, IDependencyListener dependencyListener, SharedImages images) {
        super(TextTemplateContextType.getInstance(), configs, repos, pcProvider, dependencyListener, images);
    }

    @Override
    protected Set<DependencyInfo> calculateAvailableDependencies(TextContentAssistInvocationContext context) {
        IJavaProject project = context.getProject().orNull();
        if (project == null) {
            return Collections.emptySet();
        } else {
            IJavaProject javaProject = project;
            return dependencyListener
                    .getDependenciesForProject(DependencyInfos.createDependencyInfoForProject(javaProject));
        }
    }

    @Override
    protected TemplateContext getTemplateContext(IDocument document, Position position) {
        return new DocumentTemplateContext(templateContextType, document, position);
    }

    @Override
    protected Location getLocation() {
        return Location.FILE;
    }
}
