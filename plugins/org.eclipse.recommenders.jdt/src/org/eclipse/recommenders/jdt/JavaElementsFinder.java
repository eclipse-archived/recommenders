/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.jdt;

import static com.google.common.base.Optional.*;
import static org.eclipse.jdt.core.IPackageFragmentRoot.K_SOURCE;
import static org.eclipse.recommenders.internal.jdt.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@SuppressWarnings("restriction")
public class JavaElementsFinder {

    public static ImmutableList<IJavaProject> findAccessibleJavaProjects() {
        Builder<IJavaProject> b = ImmutableList.builder();
        try {
            JavaModelManager mgr = JavaModelManager.getJavaModelManager();
            b.add(mgr.getJavaModel().getJavaProjects());
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_JAVA_PROJECTS, e);
        }
        return b.build();
    }

    public static ImmutableList<IPackageFragmentRoot> findSourceRoots(IJavaProject project) {
        Builder<IPackageFragmentRoot> b = ImmutableList.builder();
        try {
            for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
                if (K_SOURCE == root.getKind()) {
                    b.add(root);
                }
            }
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOTS, e, project);
        }
        return b.build();
    }

    public static ImmutableList<IPackageFragment> findPackages(IPackageFragmentRoot root) {
        Builder<IPackageFragment> b = ImmutableList.builder();
        try {
            for (IJavaElement e : root.getChildren()) {
                b.add((IPackageFragment) e);
            }
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT, e, root);
        }
        return b.build();
    }

    public static ImmutableList<ICompilationUnit> findCompilationUnits(IPackageFragment fragment) {
        Builder<ICompilationUnit> b = ImmutableList.builder();
        try {
            b.add(fragment.getCompilationUnits());
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_COMPILATION_UNITS, e, fragment);
        }
        return b.build();
    }

    public static Optional<IType> findType(String typename, IJavaProject project) {
        try {
            return fromNullable(project.findType(typename));
        } catch (JavaModelException e) {
            log(ERROR_CANNOT_FIND_TYPE_IN_PROJECT, e, typename, project);
            return absent();
        }
    }

    /**
     * Returns the compilation unit's absolute location on the local hard drive - if it exists.
     */
    public static Optional<File> findLocation(@Nullable ICompilationUnit cu) {
        if (cu == null) {
            return absent();
        }
        IResource resource = cu.getResource();
        if (resource == null) {
            return absent();
        }
        IPath location = resource.getLocation();
        if (location == null) {
            return absent();
        }
        File file = location.toFile();
        if (!file.exists()) {
            return absent();
        }
        return Optional.of(file);
    }
}
