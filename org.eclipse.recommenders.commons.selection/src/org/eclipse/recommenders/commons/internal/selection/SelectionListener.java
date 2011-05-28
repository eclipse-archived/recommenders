/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.commons.internal.selection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.JavaElementSelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

@SuppressWarnings("restriction")
final class SelectionListener implements ISelectionListener {

    private final IPartListener partListener;
    private final SelectionContextResolver contextResolver;
    private ISelection lastSelection;

    private final Set<IExtendedSelectionListener> listeners = new HashSet<IExtendedSelectionListener>();

    public SelectionListener(final IWorkbenchWindow win, final SelectionContextResolver contextResolver) {
        partListener = new PartListener(new CursorListener(this, win));
        this.contextResolver = contextResolver;
    }

    protected void update(final IWorkbenchPart part, final ISelection selection) {
        if (!selection.equals(lastSelection)) {
            lastSelection = selection;

            try {
                final JavaElementSelection context = contextResolver.resolve(part, selection);
                if (!SelectionPlugin.isStarted()) {
                    SelectionPlugin.start(part.getSite().getPage());
                }
                notifyListeners(context);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void notifyListeners(final JavaElementSelection context) {
        if (context != null) {
            for (final IExtendedSelectionListener listener : listeners) {
                listener.update(context);
            }
        }
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (!(part instanceof CompilationUnitEditor)) {
            update(part, selection);
        }
    }

    public IPartListener getPartListener() {
        return partListener;
    }

    public void addListener(final IExtendedSelectionListener listener) {
        listeners.add(listener);
    }
}
