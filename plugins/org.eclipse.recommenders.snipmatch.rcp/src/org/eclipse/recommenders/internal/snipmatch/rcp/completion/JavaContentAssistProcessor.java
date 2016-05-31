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

import static org.eclipse.jdt.ui.text.IJavaPartitions.*;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.rcp.DependencyInfos;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.LogMessages;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("restriction")
public class JavaContentAssistProcessor extends AbstractContentAssistProcessor<JavaContentAssistInvocationContext> {

    @Inject
    public JavaContentAssistProcessor(SnippetRepositoryConfigurations configs, Repositories repos,
            IProjectCoordinateProvider pcProvider, IDependencyListener dependencyListener, SharedImages images) {
        super(JavaTemplateContextType.getInstance(), configs, repos, pcProvider, dependencyListener, images);
    }

    @Override
    protected Set<DependencyInfo> calculateAvailableDependencies(JavaContentAssistInvocationContext context) {
        IJavaProject javaProject = context.getProject();
        if (javaProject == null) {
            return Collections.emptySet();
        }

        DependencyInfo dependencyInfo = DependencyInfos.createProjectDependencyInfo(javaProject).orNull();
        if (dependencyInfo == null) {
            return Collections.emptySet();
        } else {
            return dependencyListener.getDependenciesForProject(dependencyInfo);
        }
    }

    @Override
    protected TemplateContext getTemplateContext(IDocument document, Position position) {
        ICompilationUnit cu = context.getCompilationUnit();
        JavaContext javaTemplateContext = new JavaContext(templateContextType, document, position, cu);
        javaTemplateContext.setForceEvaluation(true);
        return javaTemplateContext;
    }

    @Override
    protected Location getLocation() {
        try {
            String partition = TextUtilities.getContentType(context.getDocument(), JAVA_PARTITIONING,
                    context.getInvocationOffset(), true);
            return getLocation(context, partition);
        } catch (BadLocationException e) {
            Logs.log(LogMessages.ERROR_CANNOT_COMPUTE_LOCATION, e);
            return Location.JAVA_FILE;
        }
    }

    @VisibleForTesting
    static Location getLocation(ContentAssistInvocationContext context, String partition) {
        if (partition.equals(JAVA_DOC)) {
            return Location.JAVADOC;
        }
        if (partition.equals(JAVA_SINGLE_LINE_COMMENT) || partition.equals(JAVA_MULTI_LINE_COMMENT)) {
            return Location.JAVA_FILE;
        }
        JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
        CompletionContext coreContext = javaContext.getCoreContext();
        if (coreContext == null) {
            return Location.JAVA_FILE;
        }
        if (coreContext.isInJavadoc()) {
            return Location.JAVADOC;
        }
        int tokenLocation = coreContext.getTokenLocation();
        if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
            return Location.JAVA_TYPE_MEMBERS;
        }
        if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
            return Location.JAVA_STATEMENTS;
        }
        return Location.JAVA_FILE;
    }
}
