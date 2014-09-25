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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class CompletionHandler extends AbstractHandler {

    private SnipmatchCompletionEngine engine;

    private <T> T request(Class<T> clazz) {
        return InjectionService.getInstance().requestInstance(clazz);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (!(editor instanceof JavaEditor)) {
            return null;
        }
        JavaEditor ed = (JavaEditor) editor;
        if (ed.isEditorInputReadOnly()) {
            return null;
        }
        ISourceViewer viewer = ed.getViewer();
        int offset = viewer.getSelectedRange().x;
        JavaContentAssistInvocationContext ctx = new JavaContentAssistInvocationContext(viewer, offset, ed);
        if (engine == null) {
            engine = request(SnipmatchCompletionEngine.class);
        }
        engine.show(ctx);
        return null;
    }
}
