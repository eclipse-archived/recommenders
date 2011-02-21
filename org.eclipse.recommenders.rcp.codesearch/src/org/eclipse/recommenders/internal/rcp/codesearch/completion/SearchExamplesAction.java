/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.completion;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendCodeSearchRequestJob;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

@SuppressWarnings("restriction")
public class SearchExamplesAction implements IEditorActionDelegate {
    private JavaEditor editor;
    private IJavaProject javaProject;

    @Override
    public void run(final IAction action) {
        if (editor == null) {
            return;
        }
        try {
            final Request request = createSearchRequestFromSource();
            new SendCodeSearchRequestJob(request, javaProject).schedule();
        } catch (final Exception e) {
            CodesearchPlugin.logError(e, "Creating search request failed.");
        }
    }

    private Request createSearchRequestFromSource() throws JavaModelException {
        final ASTNode activeDeclarationNode = CrASTUtil.resolveClosestTypeDeclarationNode(editor);
        if (activeDeclarationNode == null) {
            return Request.INVALID;
        }
        final ITextSelection selection = RCPUtils.getTextSelection(editor);
        final SearchRequestCreator searchRequestCreator = new SearchRequestCreator(activeDeclarationNode, selection);
        final Request request = searchRequestCreator.getRequest();
        request.uniqueUserId = UUIDHelper.getUUID();
        request.uniqueRequestId = UUIDHelper.generateUID();
        return request;
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
    }

    @Override
    public void setActiveEditor(final IAction action, final IEditorPart editorPart) {
        if (editorPart instanceof JavaEditor) {
            this.editor = (JavaEditor) editorPart;
            this.javaProject = EditorUtility.getJavaProject(editor.getEditorInput());
        } else {
            this.editor = null;
            this.javaProject = null;
        }
    }
}
