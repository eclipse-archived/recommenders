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
package org.eclipse.recommenders.completion.rcp;

import static com.google.common.base.Optional.*;
import static com.google.common.base.Throwables.propagate;
import static org.eclipse.recommenders.utils.Checks.*;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;

@Beta
@SuppressWarnings("restriction")
public class CompletionContexts {

    /**
     * Creates a content assist invocation context from a quick fix invocation context.
     */
    public static JavaContentAssistInvocationContext toContentAssistInvocationContext(IInvocationContext context) {
        ensureIsNotNull(context);
        return new QuickFixToContentAssistContextFunction().apply(context);
    }

    /**
     * Creates a simple (i.e., not extended) completion context.
     * <p>
     * This context may be used for snippet completions.
     */
    public static Optional<JavaContentAssistInvocationContext> newContentAssistInvocationContext(IEditorPart editor) {
        if (editor instanceof JavaEditor) {
            JavaEditor ed = (JavaEditor) editor;
            ISourceViewer viewer = ed.getViewer();
            int offset = viewer.getTextWidget().getCaretOffset();
            return of(new JavaContentAssistInvocationContext(viewer, offset, ed));
        }
        return absent();
    }

    static final class QuickFixToContentAssistContextFunction implements
            Function<IInvocationContext, JavaContentAssistInvocationContext> {
        private CompletionContext internalContext;

        @Override
        public JavaContentAssistInvocationContext apply(IInvocationContext context) {
            ICompilationUnit cu = context.getCompilationUnit();
            int offset = context.getSelectionOffset();
            try {
                cu.codeComplete(offset, new CompletionRequestor() {
                    @Override
                    public void acceptContext(CompletionContext context) {
                        internalContext = context;
                    }

                    @Override
                    public boolean isExtendedContextRequired() {
                        return true;
                    }

                    @Override
                    public void accept(CompletionProposal proposal) {
                    }
                });
            } catch (JavaModelException e) {
                propagate(e);
            }

            JavaEditor editor = cast(EditorUtility.isOpenInEditor(cu));
            ISourceViewer viewer = editor.getViewer();
            return new JavaContentAssistInvocationContext(viewer, offset, editor) {
                @Override
                public CompletionContext getCoreContext() {
                    return internalContext;
                }
            };
        }
    }
}
