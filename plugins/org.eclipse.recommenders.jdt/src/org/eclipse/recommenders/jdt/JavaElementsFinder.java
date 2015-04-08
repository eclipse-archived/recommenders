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
import static org.eclipse.jdt.core.IPackageFragmentRoot.*;
import static org.eclipse.recommenders.internal.jdt.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
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

    public static ImmutableList<IPackageFragmentRoot> findPackageRootsWithSources(IJavaProject project) {
        Builder<IPackageFragmentRoot> b = ImmutableList.builder();
        try {
            for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
                if (K_SOURCE == root.getKind()) {
                    b.add(root);
                } else if (K_BINARY == root.getKind()) {
                    if (hasSourceAttachment(root)) {
                        b.add(root);
                    }
                }
            }
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOTS, e, project);
        }
        return b.build();
    }

    public static ImmutableList<IPackageFragmentRoot> findPackageFragmentRoots(IJavaProject project) {
        Builder<IPackageFragmentRoot> b = ImmutableList.builder();
        try {
            for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
                b.add(root);
            }
        } catch (JavaModelException e) {
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

    public static ImmutableList<IClassFile> findClassFiles(IPackageFragment fragment) {
        Builder<IClassFile> b = ImmutableList.builder();
        try {
            b.add(fragment.getClassFiles());
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_CLASS_FILES, e, fragment);
        }
        return b.build();
    }

    public static Optional<IType> findType(String typename, IJavaProject project) {
        try {
            return fromNullable(project.findType(typename));
        } catch (Exception e) {
            log(ERROR_CANNOT_FIND_TYPE_IN_PROJECT, e, typename, project);
            return absent();
        }
    }

    public static ImmutableList<IType> findTypes(IJavaProject project) {
        Builder<IType> b = ImmutableList.builder();
        for (ITypeRoot root : findTypeRoots(project)) {
            try {
                if (root instanceof ICompilationUnit) {
                    for (IType type : ((ICompilationUnit) root).getTypes()) {
                        b.add(type);
                    }
                } else if (root instanceof IClassFile) {
                    b.add(((IClassFile) root).getType());
                }
            } catch (JavaModelException e) {
                log(ERROR_CANNOT_FETCH_TYPES, e, root);
            }
        }
        return b.build();
    }

    public static ImmutableList<ITypeRoot> findTypeRoots(IJavaProject project) {
        Builder<ITypeRoot> b = ImmutableList.builder();
        for (IPackageFragmentRoot root : findPackageFragmentRoots(project)) {
            b.addAll(findTypeRoots(root));
        }
        return b.build();
    }

    public static ImmutableList<ITypeRoot> findTypeRoots(IPackageFragmentRoot root) {
        Builder<ITypeRoot> b = ImmutableList.builder();
        for (IPackageFragment pkg : findPackages(root)) {
            b.addAll(findTypeRoots(pkg));
        }
        return b.build();
    }

    public static ImmutableList<ITypeRoot> findTypeRoots(IPackageFragment fragment) {
        Builder<ITypeRoot> b = ImmutableList.builder();
        ImmutableList<ICompilationUnit> cus = findCompilationUnits(fragment);
        ImmutableList<IClassFile> classFiles = findClassFiles(fragment);
        b.addAll(cus);
        b.addAll(classFiles);
        return b.build();
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

    /**
     * Returns the compilation unit's absolute location on the local hard drive - if it exists.
     */
    public static Optional<File> findLocation(@Nullable IPackageFragmentRoot root) {
        if (root == null) {
            return absent();
        }
        File res = null;

        final IResource resource = root.getResource();
        if (resource != null) {
            if (resource.getLocation() == null) {
                res = resource.getRawLocation().toFile().getAbsoluteFile();
            } else {
                res = resource.getLocation().toFile().getAbsoluteFile();
            }
        }
        if (root.isExternal()) {
            res = root.getPath().toFile().getAbsoluteFile();
        }

        // if the file (for whatever reasons) does not exist return absent().
        if (res != null && !res.exists()) {
            return absent();
        }
        return fromNullable(res);
    }

    public static boolean hasSourceAttachment(IPackageFragmentRoot fragmentRoot) {
        try {
            return fragmentRoot.getSourceAttachmentPath() != null;
        } catch (Exception e) {
            log(ERROR_CANNOT_FETCH_SOURCE_ATTACHMENT_PATH, e, fragmentRoot);
            return false;
        }
    }
}
