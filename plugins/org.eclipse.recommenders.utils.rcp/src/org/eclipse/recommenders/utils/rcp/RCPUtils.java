/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.utils.rcp;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.collect.Sets;

public class RCPUtils {
    public static StructuredSelection asStructuredSelection(final ISelection selection) {
        return (StructuredSelection) (isStructured(selection) ? selection : StructuredSelection.EMPTY);
    }

    public static boolean isStructured(final ISelection selection) {
        return selection instanceof IStructuredSelection;
    }

    public static <T> List<T> toList(final ISelection selection) {
        return asStructuredSelection(selection).toList();
    }

    public static <T> T unsafeFirstElement(final ISelection s) {
        return (T) asStructuredSelection(s).getFirstElement();
    }

    public static <T> T safeFirstElement(final ISelection s, final Class<T> type) {
        final Object element = asStructuredSelection(s).getFirstElement();
        return (T) (type.isInstance(element) ? element : null);
    }

    public static IWorkbenchPage getActiveWorkbenchPage() {
        ensureIsNotNull(Display.getCurrent(), "not called from ui thread");
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return null;
        }
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        final IWorkbenchPage page = window.getActivePage();
        return page;
    }

    public static ITextSelection getTextSelection(final ITextEditor editor) {
        if (editor == null) {
            return new TextSelection(0, 0);
        } else {
            return (TextSelection) editor.getSelectionProvider().getSelection();
        }
    }

    public static Set<IProject> getAllOpenProjects() {
        final Set<IProject> result = Sets.newHashSet();
        final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (final IProject project : projects) {
            if (project.isAccessible()) {
                result.add(project);
            }
        }

        return result;
    }

}
