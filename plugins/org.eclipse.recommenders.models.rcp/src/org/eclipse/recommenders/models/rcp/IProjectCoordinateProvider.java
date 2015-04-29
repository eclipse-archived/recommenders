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
package org.eclipse.recommenders.models.rcp;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueMethodName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

/**
 * Resolves an IJavaElement or AST binding to its project coordinate.
 */
public interface IProjectCoordinateProvider {

    Optional<ProjectCoordinate> resolve(ITypeBinding binding);

    Optional<ProjectCoordinate> resolve(IType type);

    Optional<ProjectCoordinate> resolve(IMethodBinding binding);

    Optional<ProjectCoordinate> resolve(IMethod method);

    Optional<ProjectCoordinate> resolve(IPackageFragmentRoot root);

    Optional<ProjectCoordinate> resolve(IJavaProject javaProject);

    Optional<ProjectCoordinate> resolve(DependencyInfo info);

    Result<ProjectCoordinate> tryResolve(DependencyInfo info);

    // TODO: convenience method to save a few lines of code to get from an IDE element to a qualified name
    ITypeName toName(IType type);

    Optional<UniqueTypeName> toUniqueName(IType type);

    Result<UniqueTypeName> tryToUniqueName(IType type);

    Optional<IMethodName> toName(IMethod method);

    Optional<UniqueMethodName> toUniqueName(IMethod method);

    Result<UniqueMethodName> tryToUniqueName(IMethod method);

}
