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

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class MethodReturnsLocationFinder {

    public static MethodReturnsLocationFinder find(final ASTNode cu, final Set<ITypeName> expectedTypes) {
        try {
            return new MethodReturnsLocationFinder(cu, expectedTypes);
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Set<ASTNode> methods = Sets.newHashSet();
    private final Set<ASTNode> types = Sets.newHashSet();

    public Set<ASTNode> getMethodReturnSimpleNameNodes() {
        return methods;
    }

    public MethodReturnsLocationFinder(final ASTNode member, final Set<ITypeName> expectedTypes) {
        ensureIsNotNull(member);
        ensureIsNotNull(expectedTypes);
        member.accept(new ASTVisitor(false) {

            @Override
            public boolean visit(final SuperMethodInvocation node) {
                final IMethodBinding b = node.resolveMethodBinding();
                addMethodBinding(b, node);
                return true;
            }

            private void addVariableBinding(final IVariableBinding b, final SimpleName node) {
                final ITypeName type = BindingUtils.toTypeName(b.getType()).orNull();
                if (expectedTypes.contains(type)) {
                    types.add(node);
                }
            }

            private void addMethodBinding(final IMethodBinding b, final ASTNode node) {
                final IMethodName method = BindingUtils.toMethodName(b).orNull();
                if (method == null) {
                    return;
                }
                final ITypeName returnType = method.getReturnType();
                if (expectedTypes.contains(returnType)) {
                    methods.add(node);
                }
            }
        });
    }
}
