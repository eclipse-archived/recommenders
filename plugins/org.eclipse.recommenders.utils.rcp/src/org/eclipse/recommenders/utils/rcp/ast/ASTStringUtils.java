/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp.ast;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ASTStringUtils {
    @Inject
    private static JavaElementResolver resolver;

    @SuppressWarnings("unchecked")
    public static String toDeclarationString(final TypeDeclaration type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toQualifiedString(type));
        final Type superclassType = type.getSuperclassType();
        if (superclassType != null) {
            sb.append(" extends ").append(toSimpleString(superclassType));
        }
        final List<Type> interfaces = type.superInterfaceTypes();
        if (!interfaces.isEmpty()) {
            sb.append(" implements ");
            for (final Type interfaceType : interfaces) {
                sb.append(toSimpleString(interfaceType)).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        final String string = sb.toString();
        return string;
    }

    public static String toSimpleString(final IMethodName method) {
        final StringBuilder sb = new StringBuilder();
        if (method.isInit()) {
            sb.append("new ").append(method.getDeclaringType().getClassName());
        } else {
            sb.append(method.getName());
        }
        sb.append("(");
        if (method.hasParameters()) {
            sb.append("..");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String toQualifiedString(final TypeDeclaration type) {
        final ITypeBinding typeBinding = type.resolveBinding();
        if (typeBinding != null) {
            return toQualifiedString(typeBinding);
        }
        final StringBuilder sb = new StringBuilder();
        final ASTNode parent = type.getParent();
        if (parent instanceof CompilationUnit) {
            final CompilationUnit cu = (CompilationUnit) parent;
            final PackageDeclaration package1 = cu.getPackage();
            final String fullyQualifiedName = package1.getName().getFullyQualifiedName();
            return sb.append(fullyQualifiedName).append(".").append(type.getName()).toString();
        }
        sb.append("[unresolved] " + type.getName());
        return sb.toString();
    }

    public static String toSimpleString(final Type type) {
        if (type.isQualifiedType()) {
            final QualifiedType qualifiedType = (QualifiedType) type;
            return qualifiedType.getName().toString();
        }
        return type.toString();
    }

    @SuppressWarnings("unchecked")
    public static String toQualifiedString(final MethodDeclaration method) {
        ensureIsNotNull(method);
        final StringBuilder sb = new StringBuilder();
        final ASTNode parent = method.getParent();
        if (parent instanceof TypeDeclaration) {
            final TypeDeclaration declaringType = (TypeDeclaration) parent;
            sb.append(toQualifiedString(declaringType)).append(".");
        } else if (parent instanceof AnonymousClassDeclaration) {
            final AnonymousClassDeclaration anonymous = (AnonymousClassDeclaration) parent;
            final String string = toQualifiedString(anonymous.resolveBinding());
            sb.append(string).append(".");
        }

        sb.append(method.getName()).append("(");
        for (final SingleVariableDeclaration param : (List<SingleVariableDeclaration>) method.parameters()) {
            final Type type = param.getType();
            sb.append(toSimpleString(type)).append(", ");
        }
        if (!method.parameters().isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(")");
        return sb.toString();
    }

    public static String toSimpleString(final ITypeBinding binding) {
        return binding.getName();
    }

    public static String toQualifiedString(@Nullable final ITypeBinding binding) {
        if (binding == null) {
            return null;
        }
        if (binding.isAnonymous()) {
            final String binaryName = binding.getBinaryName();
            return binaryName != null ? binaryName : toQualifiedString(binding.getDeclaringClass()) + "$<>";
        }
        return binding.getQualifiedName();
    }

    public static String toDeclarationString(final ITypeBinding binding) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toQualifiedString(binding));
        final ITypeBinding superclass = binding.getSuperclass();
        if (superclass != null) {
            sb.append(" extends " + toSimpleString(superclass));
        }
        return sb.toString();
    }

    public static List<String> toSimpleTypesString(final Set<ITypeBinding> types) {
        final List<String> result = Lists.newLinkedList();
        for (final ITypeBinding b : types) {
            final String simpleName = toSimpleString(b);
            result.add(simpleName);
        }
        return result;
    }

    public static List<String> toSimpleMethodString(final Set<IMethodBinding> methods) {
        final List<String> result = Lists.newLinkedList();
        for (final IMethodBinding method : methods) {
            final String simpleName = toSimpleMethodString(method);
            result.add(simpleName);
        }
        return result;
    }

    private static String toSimpleMethodString(final IMethodBinding method) {
        final IMethod jdtMethod = (IMethod) method.getJavaElement();
        final IMethodName jdt2crMethod = resolver.toRecMethod(jdtMethod).or(VmMethodName.NULL);
        final String simpleName = Names.vm2srcSimpleMethod(jdt2crMethod);
        return simpleName;
    }
}
