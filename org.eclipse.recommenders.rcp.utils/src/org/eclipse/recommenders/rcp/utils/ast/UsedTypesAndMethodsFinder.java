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
package org.eclipse.recommenders.rcp.utils.ast;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class UsedTypesAndMethodsFinder {
    public static UsedTypesAndMethodsFinder find(final ASTNode node, final Set<ITypeName> expectedTypes,
            final Set<IMethodName> expectedMethods) {
        return new UsedTypesAndMethodsFinder(node, expectedTypes, expectedMethods);
    }

    private final Set<IMethodBinding> methods = Sets.newHashSet();

    private final Set<ITypeBinding> types = Sets.newHashSet();

    public Set<IMethodBinding> getMethods() {
        return methods;
    }

    public Set<ITypeBinding> getTypes() {
        return types;
    }

    public UsedTypesAndMethodsFinder(final ASTNode member, final Set<ITypeName> expectedTypes,
            final Set<IMethodName> expectedMethods) {
        ensureIsNotNull(member);
        ensureIsNotNull(expectedTypes);
        member.accept(new ASTVisitor(false) {
            @Override
            public boolean visit(final SimpleName node) {
                final IBinding b = node.resolveBinding();
                if (b instanceof IVariableBinding) {
                    addVariableBinding((IVariableBinding) b, node);
                } else if (b instanceof IMethodBinding) {
                    addMethodBinding((IMethodBinding) b);
                }
                return true;
            }

            @Override
            public boolean visit(final ClassInstanceCreation node) {
                final IMethodBinding b = node.resolveConstructorBinding();
                addMethodBinding(b);
                return true;
            }

            @Override
            public boolean visit(final SimpleType node) {
                final ITypeBinding b = node.resolveBinding();
                addTypeBinding(b);
                return true;
            }

            @Override
            public boolean visit(final SuperConstructorInvocation node) {
                final IMethodBinding b = node.resolveConstructorBinding();
                addMethodBinding(b);
                return true;
            }

            @Override
            public boolean visit(final ConstructorInvocation node) {
                final IMethodBinding b = node.resolveConstructorBinding();
                addMethodBinding(b);
                return true;
            }

            @Override
            public boolean visit(final SuperMethodInvocation node) {
                final IMethodBinding b = node.resolveMethodBinding();
                addMethodBinding(b);
                return true;
            }

            /**
             * returns true iff the give type was relevant, and thus added to the list of returned types
             */
            private boolean addTypeBinding(final ITypeBinding b) {
                final ITypeName type = BindingUtils.toTypeName(b);
                if (expectedTypes.contains(type)) {
                    types.add(b);
                    return true;
                }
                return false;
            }

            private void addVariableBinding(final IVariableBinding b, final SimpleName node) {
                final ITypeBinding varType = b.getType();
                final ITypeName type = BindingUtils.toTypeName(varType);
                if (expectedTypes.contains(type)) {
                    types.add(varType);
                }
            }

            private void addMethodBinding(final IMethodBinding b) {
                final IMethodName method = BindingUtils.toMethodName(b);
                if (method == null) {
                    return;
                }
                if (expectedMethods.contains(method)) {
                    methods.add(b);
                }
                handleMethodsDeclaringType(expectedTypes, b, method);
                handleMethodsReturnType(b);
                handleMethodsParameterTypes(b);
            }

            private void handleMethodsDeclaringType(final Set<ITypeName> expectedTypes, final IMethodBinding b,
                    final IMethodName method) {
                final ITypeBinding declaringClass = b.getDeclaringClass();
                if (addTypeBinding(declaringClass)) {
                    // if the type is interesting but the method itself is not
                    // in the wish list -
                    // add it anyway:
                    methods.add(b);
                }
            }

            private void handleMethodsReturnType(final IMethodBinding b) {
                final ITypeBinding returnType = b.getReturnType();
                addTypeBinding(returnType);
            }

            private void handleMethodsParameterTypes(final IMethodBinding b) {
                for (final ITypeBinding param : b.getParameterTypes()) {
                    addTypeBinding(param);
                }
            }
        });
    }
}
