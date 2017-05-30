/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class CompletionHandler extends AbstractHandler {

    private <T> T request(Class<T> clazz) {
        return InjectionService.getInstance().requestInstance(clazz);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = getActiveEditor(event).orNull();
        if (editor == null) {
            return null;
        }

        if (!(editor instanceof ITextEditor)) {
            return null;
        }

        ITextEditor textEditor = (ITextEditor) editor;
        if (!textEditor.isEditable()) {
            return null;
        }

        IPersistableElement persistable = textEditor.getEditorInput().getPersistable();
        if (persistable == null) {
            return null;
        }

        Method getSourceViewerMethod = Reflections.getDeclaredMethod(AbstractTextEditor.class, "getSourceViewer")
                .orNull();
        if (getSourceViewerMethod == null) {
            throw new ExecutionException(Messages.ERROR_UNABLE_TO_DETERMINE_SOURCE_VIEWER);
        }

        ISourceViewer viewer;
        try {
            viewer = (ISourceViewer) getSourceViewerMethod.invoke(textEditor);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExecutionException(Messages.ERROR_UNABLE_TO_DETERMINE_SOURCE_VIEWER, e);
        }

        int offset = viewer.getSelectedRange().x;

        String filename = null;
        if (persistable instanceof IPathEditorInput) {
            IPathEditorInput editorInput = (IPathEditorInput) persistable;
            filename = editorInput.getPath().toFile().getName();
        } else if (persistable instanceof FileStoreEditorInput) {
            FileStoreEditorInput editorInput = (FileStoreEditorInput) persistable;
            try {
                filename = new File(editorInput.getURI()).getName();
            } catch (IllegalArgumentException e) {
                // Not a file, ignore
            }
        }

        SnipmatchCompletionEngine<? extends ContentAssistInvocationContext> engine;
        if (textEditor instanceof JavaEditor) {
            engine = createCompletionEngineForJava(textEditor, viewer, offset, filename);
        } else {
            engine = createCompletionEngineForText(textEditor.getEditorInput(), viewer, offset, filename);
        }

        engine.show();

        return null;
    }

    private Optional<IEditorPart> getActiveEditor(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof FormEditor) {
            FormEditor formEditor = (FormEditor) editor;
            return Optional.fromNullable(formEditor.getActiveEditor());
        } else if (editor instanceof MultiPageEditorPart) {
            MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) editor;
            Object selectedPage = multiPageEditorPart.getSelectedPage();
            if (selectedPage instanceof IEditorPart) {
                return Optional.fromNullable((IEditorPart) selectedPage);
            } else {
                return Optional.absent();
            }
        } else {
            return Optional.fromNullable(editor);
        }
    }

    private SnipmatchCompletionEngine<? extends ContentAssistInvocationContext> createCompletionEngineForJava(
            IEditorPart editor, ISourceViewer viewer, int offset, String filename) {
        JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(viewer, offset, editor);
        JavaContentAssistProcessor processor = request(JavaContentAssistProcessor.class);

        return new SnipmatchCompletionEngine<JavaContentAssistInvocationContext>(context, processor, filename,
                request(EventBus.class), request(ColorRegistry.class), request(FontRegistry.class));
    }

    private SnipmatchCompletionEngine<? extends ContentAssistInvocationContext> createCompletionEngineForText(
            IEditorInput input, ISourceViewer viewer, int offset, String filename) {
        IJavaProject javaProject = EditorUtility.getJavaProject(input);

        TextContentAssistInvocationContext context = new TextContentAssistInvocationContext(viewer, offset,
                javaProject);
        TextContentAssistProcessor processor = request(TextContentAssistProcessor.class);

        return new SnipmatchCompletionEngine<TextContentAssistInvocationContext>(context, processor, filename,
                request(EventBus.class), request(ColorRegistry.class), request(FontRegistry.class));
    }
}
