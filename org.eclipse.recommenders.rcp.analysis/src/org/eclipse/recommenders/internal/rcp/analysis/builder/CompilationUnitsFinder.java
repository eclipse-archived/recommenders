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
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.collect.Lists;

public class CompilationUnitsFinder implements IResourceVisitor {

    public static List<ICompilationUnit> visitResource(final IResource resource) throws CoreException {
        final CompilationUnitsFinder v = new CompilationUnitsFinder();
        resource.accept(v);
        return v.cus;
    }

    List<ICompilationUnit> cus = Lists.newLinkedList();

    @Override
    public boolean visit(final IResource resource) throws CoreException {
        if (!isFile(resource)) {
            return true;
        }

        final IJavaElement element = JavaCore.create(resource);
        if (element instanceof ICompilationUnit) {
            final ICompilationUnit cu = (ICompilationUnit) element;
            try {
                cu.isStructureKnown();
                cus.add(cu);
            } catch (final Exception e) {
                // if we can't open it, skip it
            }

        }
        return true;
    }

    private boolean isFile(final IResource resource) {
        return resource.getType() == IResource.FILE;
    }
}
