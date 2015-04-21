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
package org.eclipse.recommenders.rcp.utils;

import static com.google.common.base.Optional.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

@SuppressWarnings({ "unchecked" })
public final class Selections {

    private Selections() {
        // Not meant to be instantiated
    }

    public static <T> Optional<T> getFirstSelected(OpenEvent e) {
        ISelection selection = e.getViewer().getSelection();
        return getFirstSelected(selection);
    }

    public static <T> Optional<T> getFirstSelected(Viewer viewer) {
        ISelection selection = viewer.getSelection();
        return getFirstSelected(selection);
    }

    public static IStructuredSelection asStructuredSelection(final ISelection selection) {
        return (IStructuredSelection) (isStructured(selection) ? selection : StructuredSelection.EMPTY);
    }

    public static boolean isStructured(final ISelection selection) {
        return selection instanceof IStructuredSelection;
    }

    public static <T> List<T> toList(final ISelection selection) {
        return asStructuredSelection(selection).toList();
    }

    public static <T> Optional<T> safeFirstElement(final ISelection s, final Class<T> type) {
        final Object element = asStructuredSelection(s).getFirstElement();
        return (Optional<T>) (type.isInstance(element) ? of(element) : absent());
    }

    public static <T> Optional<T> getFirstSelected(final ISelection s) {
        final T element = unsafeFirstElement(s);
        return Optional.fromNullable(element);
    }

    public static <T> T unsafeFirstElement(final ISelection s) {
        return (T) asStructuredSelection(s).getFirstElement();
    }

    public static <T> Set<T> toSet(ISelection selection) {
        List<T> list = toList(selection);
        return Sets.newHashSet(list);
    }

}
