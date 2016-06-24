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
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.eclipse.jdt.core.IJavaElement.*;
import static org.eclipse.jdt.core.IPackageFragmentRoot.*;
import static org.eclipse.jdt.core.compiler.CharOperation.charToString;
import static org.eclipse.recommenders.internal.jdt.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;
import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.recommenders.internal.jdt.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@SuppressWarnings("restriction")
public final class JavaElementsFinder {

    private JavaElementsFinder() {
        // Not meant to be instantiated
    }

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
                if (K_SOURCE == getPackageFragmentRootKind(root)) {
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
                int kind = getPackageFragmentRootKind(root);
                if (K_SOURCE == kind) {
                    b.add(root);
                } else if (K_BINARY == kind) {
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
            b.addAll(findTypes(root));
        }
        return b.build();
    }

    public static ImmutableList<IType> findTypes(IPackageFragmentRoot root) {
        Builder<IType> b = ImmutableList.builder();
        for (ITypeRoot typeRoot : findTypeRoots(root)) {
            b.addAll(findTypes(typeRoot));
        }
        return b.build();
    }

    public static ImmutableList<IType> findTypes(ITypeRoot root) {
        Builder<IType> b = ImmutableList.builder();
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

    /**
     *
     * @param typeSignature
     *            e.g., QList;
     * @param enclosing
     * @return
     */
    public static Optional<ITypeName> resolveType(char[] typeSignature, @Nullable IJavaElement enclosing) {
        typeSignature = CharOperation.replaceOnCopy(typeSignature, '.', '/');
        VmTypeName res = null;
        try {
            int dimensions = Signature.getArrayCount(typeSignature);
            outer: switch (typeSignature[dimensions]) {

            case Signature.C_BOOLEAN:
            case Signature.C_BYTE:
            case Signature.C_CHAR:
            case Signature.C_DOUBLE:
            case Signature.C_FLOAT:
            case Signature.C_INT:
            case Signature.C_LONG:
            case Signature.C_SHORT:
            case Signature.C_VOID:
                // take the whole string including any arrays
                res = VmTypeName.get(new String(typeSignature, 0, typeSignature.length));
                break;
            case Signature.C_RESOLVED:
                // take the whole string including any arrays but remove the trailing ';'
                res = VmTypeName.get(new String(typeSignature, 0, typeSignature.length - 1 /* ';' */));
                break;
            case Signature.C_UNRESOLVED:
                if (enclosing == null) {
                    break;
                }
                // take the whole string (e.g. QList; or [QList;)
                String unresolved = new String(typeSignature, dimensions + 1,
                        typeSignature.length - (dimensions + 2 /* 'Q' + ';' */));
                IType ancestor = (IType) enclosing.getAncestor(IJavaElement.TYPE);
                if (ancestor == null) {
                    break;
                }
                final String[][] resolvedNames = ancestor.resolveType(unresolved);
                if (isEmpty(resolvedNames)) {
                    break;
                }
                String array = repeat('[', dimensions);
                final String pkg = resolvedNames[0][0].replace('.', '/');
                final String name = resolvedNames[0][1].replace('.', '$');
                res = VmTypeName.get(array + 'L' + pkg + '/' + name);
                break;
            case Signature.C_TYPE_VARIABLE:
                String varName = new String(typeSignature, dimensions + 1,
                        typeSignature.length - (dimensions + 2 /* 'Q' + ';' */));
                array = repeat('[', dimensions);

                for (IJavaElement cur = enclosing; cur instanceof IType
                        || cur instanceof IMethod; cur = cur.getParent()) {
                    switch (cur.getElementType()) {
                    case TYPE: {
                        IType type = (IType) cur;
                        ITypeParameter param = type.getTypeParameter(varName);
                        if (param.exists()) {
                            String[] signatures = getBoundSignatures(param);
                            if (isEmpty(signatures)) {
                                res = VmTypeName.OBJECT;
                                break outer;
                            }
                            // XXX we only consider the first type.
                            char[] append = array.concat(signatures[0]).toCharArray();
                            return resolveType(append, type);
                        }
                    }
                    case METHOD: {
                        IMethod method = (IMethod) cur;
                        ITypeParameter param = method.getTypeParameter(varName);
                        if (param.exists()) {
                            String[] signatures = getBoundSignatures(param);
                            if (isEmpty(signatures)) {
                                res = dimensions == 0 ? OBJECT
                                        : VmTypeName.get(repeat('[', dimensions) + OBJECT.getIdentifier());
                                break outer;
                            }
                            // XXX we only consider the first type.
                            char[] append = array.concat(signatures[0]).toCharArray();
                            return resolveType(append, method);
                        }
                    }
                    }
                }

                break;
            default:
                break;
            }
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_CREATE_TYPENAME, e,
                    charToString(typeSignature) + (enclosing != null ? " in " + enclosing.getElementName() : ""));
        }
        return Optional.<ITypeName>fromNullable(res);
    }

    private static String[] getBoundSignatures(ITypeParameter param) throws JavaModelException {
        String[] res = CharOperation.NO_STRINGS;
        try {
            res = param.getBoundsSignatures();
        } catch (NullPointerException e) {
            // swallow. That happened during testing in JDT Mars M6
        }
        return res;
    }

    public static IPackageFragmentRoot[] getAllPackageFragmentRoots(IJavaProject javaProject) {
        try {
            return javaProject.getAllPackageFragmentRoots();
        } catch (JavaModelException e) {
            Logs.log(LogMessages.ERROR_CANNOT_FETCH_ALL_PACKAGE_FRAGMENT_ROOTS, e, javaProject);
            return new IPackageFragmentRoot[0];
        }
    }

    public static int getPackageFragmentRootKind(IPackageFragmentRoot packageFragmentRoot) {
        try {
            return packageFragmentRoot.getKind();
        } catch (JavaModelException e) {
            Logs.log(LogMessages.ERROR_CANNOT_FETCH_PACKAGE_FRAGMENT_ROOT_KIND, e, packageFragmentRoot);
            return 0;
        }
    }
}
