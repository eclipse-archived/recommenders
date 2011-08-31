/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp.analysis.builder;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.commons.utils.Tuple;

import com.google.common.collect.Lists;

public class ChangedCompilationUnitsFinder implements IResourceDeltaVisitor {

    public static Tuple<List<ICompilationUnit>, List<ICompilationUnit>> findChangedCompilkationUnits(
            final IResourceDelta delta) throws CoreException {
        final ChangedCompilationUnitsFinder v = new ChangedCompilationUnitsFinder();
        delta.accept(v);
        return Tuple.create(v.added, v.removed);
    }

    public List<ICompilationUnit> added = Lists.newLinkedList();

    public List<ICompilationUnit> removed = Lists.newLinkedList();

    @Override
    public boolean visit(final IResourceDelta delta) throws CoreException {
        final IResource resource = delta.getResource();
        if (!isFile(resource)) {
            return true;
        }

        final IJavaElement element = JavaCore.create(resource);
        if (element instanceof ICompilationUnit) {
            final ICompilationUnit cu = (ICompilationUnit) element;
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
            case IResourceDelta.CHANGED:
            case IResourceDelta.REPLACED:
                added.add(cu);
                break;
            case IResourceDelta.REMOVED:
                removed.add(cu);
                break;
            default:
                break;
            }
        }
        return true;
    }

    private boolean isFile(final IResource resource) {
        return resource.getType() == IResource.FILE;
    }
}
