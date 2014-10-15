/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.EDITOR_ID;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindow;

import java.util.Set;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.models.rcp.ProjectCoordinateProvider;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.rcp.SnippetEditor;
import org.eclipse.recommenders.snipmatch.rcp.SnippetEditorInput;
import org.eclipse.recommenders.snipmatch.rcp.util.SnippetCodeBuilder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class CreateSnippetHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSnippetHandler.class);

    private ExecutionEvent event;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        this.event = event;
        CompilationUnitEditor editor = cast(HandlerUtil.getActiveEditor(event));
        Snippet snippet = createSnippet(editor);
        openSnippetInEditor(snippet);
        return null;
    }

    @VisibleForTesting
    Snippet createSnippet(CompilationUnitEditor editor) throws ExecutionException {
        ISourceViewer viewer = editor.getViewer();
        ITypeRoot root = cast(editor.getViewPartInput());
        CompilationUnit ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);

        IDocument doc = viewer.getDocument();
        ITextSelection textSelection = cast(viewer.getSelectionProvider().getSelection());

        ProjectCoordinateProvider pcProvider = InjectionService.getInstance().requestInstance(
                ProjectCoordinateProvider.class);

        String code = new SnippetCodeBuilder(ast, doc, textSelection).build();
        Set<ProjectCoordinate> dependencies = new DependencyExtractor(ast, textSelection, pcProvider)
                .extractDependencies();

        return new Snippet(UUID.randomUUID(),
                "", "", Lists.<String>newArrayList(), Lists.<String>newArrayList(), code, Location.NONE, dependencies); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void openSnippetInEditor(Snippet snippet) {
        IWorkbenchPage page = getActiveWorkbenchWindow(event).getActivePage();

        try {
            SnippetEditorInput input = new SnippetEditorInput(snippet);
            SnippetEditor ed = cast(page.openEditor(input, EDITOR_ID));
            ed.markDirtyUponSnippetCreation();
        } catch (PartInitException e) {
            LOG.error(Messages.ERROR_WHILE_OPENING_EDITOR, e);
            openError(HandlerUtil.getActiveShell(event), Messages.ERROR_NO_EDITABLE_REPO_FOUND,
                    Messages.ERROR_NO_EDITABLE_REPO_FOUND_HINT);
        }
    }
}
