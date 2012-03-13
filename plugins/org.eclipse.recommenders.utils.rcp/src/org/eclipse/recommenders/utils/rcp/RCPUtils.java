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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.google.common.base.Optional;

public class RCPUtils {
    public static IStructuredSelection asStructuredSelection(final ISelection selection) {
        return (IStructuredSelection) (isStructured(selection) ? selection : StructuredSelection.EMPTY);
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

    public static <T> Optional<T> safeFirstElement(final ISelection s, final Class<T> type) {
        final Object element = asStructuredSelection(s).getFirstElement();
        return (Optional<T>) (type.isInstance(element) ? of(element) : absent());
    }

    public static <T> Optional<T> first(final ISelection s) {
        final T element = unsafeFirstElement(s);
        return Optional.fromNullable(element);
    }

}
