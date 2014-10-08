/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.util.Set;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.models.rcp.Dependencies;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IDependencyListener;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.SearchContext;
import org.eclipse.recommenders.utils.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class JavaEditorSearchContext extends SearchContext {

    private static final Logger LOG = LoggerFactory.getLogger(JavaEditorSearchContext.class);

    private final JavaContentAssistInvocationContext invocationContext;

    public JavaEditorSearchContext(String searchText, JavaContentAssistInvocationContext invocationContext) {
        super(searchText, getLocation(invocationContext), getAvailableDependencies(invocationContext));
        this.invocationContext = invocationContext;
    }

    private static Set<ProjectCoordinate> getAvailableDependencies(JavaContentAssistInvocationContext invocationContext) {
        IDependencyListener dependencyListener = InjectionService.getInstance().requestInstance(
                IDependencyListener.class);

        IJavaProject project = invocationContext.getCompilationUnit().getJavaProject();
        ImmutableSet<DependencyInfo> availableDependencies = dependencyListener.getDependenciesForProject(Dependencies
                .createDependencyInfoForProject(project));

        return resolve(availableDependencies);
    }

    private static Set<ProjectCoordinate> resolve(Set<DependencyInfo> dependencyInfos) {
        Set<ProjectCoordinate> result = Sets.newHashSet();
        IProjectCoordinateProvider pcAdvisor = InjectionService.getInstance().requestInstance(
                IProjectCoordinateProvider.class);

        for (DependencyInfo dependencyInfo : dependencyInfos) {
            ProjectCoordinate pc = pcAdvisor.resolve(dependencyInfo).orNull();
            if (pc != null) {
                result.add(pc);
            }
        }

        return result;
    }

    private static Location getLocation(JavaContentAssistInvocationContext context) {
        try {
            String partition = TextUtilities.getContentType(context.getDocument(), IJavaPartitions.JAVA_PARTITIONING,
                    context.getInvocationOffset(), true);
            if (partition.equals(IJavaPartitions.JAVA_DOC)) {
                return Location.JAVADOC;
            } else {
                CompletionContext coreContext = context.getCoreContext();
                if (coreContext != null) {
                    int tokenLocation = coreContext.getTokenLocation();
                    if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
                        return Location.JAVA_TYPE_MEMBERS;
                    } else if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
                        return Location.JAVA_STATEMENTS;
                    }
                    return Location.UNKNOWN;
                }
            }
        } catch (BadLocationException e) {
            Logs.log(LogMessages.ERROR_CANNOT_COMPUTE_LOCATION, e);
        }
        return Location.FILE;
    }

    public JavaContentAssistInvocationContext getInvocationContext() {
        return invocationContext;
    }

}
