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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class UsedTypesAndMethodsLocationFinder {

    public static UsedTypesAndMethodsLocationFinder find(final CompilationUnit cu, final Set<ITypeName> expectedTypes,
            final Set<IMethodName> expectedMethods) {
        try {
            return new UsedTypesAndMethodsLocationFinder(cu, expectedTypes, expectedMethods);
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Set<ASTNode> methods = Sets.newHashSet();
    private final Set<ASTNode> types = Sets.newHashSet();

    public Set<ASTNode> getMethodSimpleNames() {
        return methods;
    }

    public Set<ASTNode> getTypeSimpleNames() {
        return types;
    }

    public UsedTypesAndMethodsLocationFinder(final ASTNode member, final Set<ITypeName> expectedTypes,
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
                    addMethodBinding((IMethodBinding) b, node);
                }
                return true;
            }

            @Override
            public boolean visit(final SuperConstructorInvocation node) {
                final IMethodBinding b = node.resolveConstructorBinding();
                addMethodBinding(b, node);
                return true;
            }

            @Override
            public boolean visit(final ConstructorInvocation node) {
                final IMethodBinding b = node.resolveConstructorBinding();
                addMethodBinding(b, node);
                return true;
            }

            @Override
            public boolean visit(final SuperMethodInvocation node) {
                final IMethodBinding b = node.resolveMethodBinding();
                addMethodBinding(b, node);
                return true;
            }

            private void addVariableBinding(final IVariableBinding b, final SimpleName node) {
                final ITypeName type = BindingUtils.toTypeName(b.getType());
                if (expectedTypes.contains(type)) {
                    types.add(node);
                }
            }

            private void addMethodBinding(final IMethodBinding b, final ASTNode node) {
                final IMethodName method = BindingUtils.toMethodName(b);
                if (method == null) {
                    return;
                }
                if (expectedTypes.contains(method.getDeclaringType())) {
                    methods.add(node);
                } else if (expectedMethods.contains(method)) {
                    methods.add(node);
                } else if (expectedTypes.contains(method.getReturnType())) {
                    methods.add(node);
                }
            }
        });
    }
}
