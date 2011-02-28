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
package org.eclipse.recommenders.internal.rcp.codecompletion.resolvers;

import static org.eclipse.recommenders.commons.utils.Checks.cast;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AstBasedVariableUsageResolver implements IVariableUsageResolver {

    private final IAstProvider astprovider;
    private int invocationOffset;

    private CompilationUnit ast;

    private MethodDeclaration astEnclosingMethodDeclaration;

    private ICompilationUnit jdtCompilationUnit;

    private IMethod jdtEnclosingMethodDeclaration;

    private Variable localVariable;

    private final Set<IMethodName> receiverMethodInvocations = Sets.newHashSet();

    @Inject
    public AstBasedVariableUsageResolver(final IAstProvider provider) {
        astprovider = provider;

    }

    @Override
    public boolean canResolve(final IIntelligentCompletionContext ctx) {
        ensureIsNotNull(ctx);
        this.localVariable = ctx.getVariable();
        this.jdtCompilationUnit = ctx.getCompilationUnit();
        this.invocationOffset = ctx.getInvocationOffset();
        if (!findAst()) {
            return false;
        }
        if (!findEnclosingMethodDeclaration()) {
            return false;
        }
        return findUsages();
    }

    private boolean findAst() {

        ast = astprovider.get(jdtCompilationUnit);
        return ast != null;
    }

    private boolean findEnclosingMethodDeclaration() {
        ensureIsNotNull(ast);
        ASTNode node = NodeFinder.perform(ast, invocationOffset, 0);
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                astEnclosingMethodDeclaration = cast(node);
                jdtEnclosingMethodDeclaration = BindingUtils.getMethod(astEnclosingMethodDeclaration.resolveBinding());
                break;
            }
            node = node.getParent();
        }
        return jdtEnclosingMethodDeclaration != null;
    }

    private boolean findUsages() {
        ensureIsNotNull(astEnclosingMethodDeclaration);
        astEnclosingMethodDeclaration.accept(new ASTVisitor() {

            @Override
            public boolean visit(final SimpleName node) {
                final IVariableBinding var = BindingUtils.getVariableBinding(node);
                if (var == null) {
                    return true;
                }
                if (var.getName().equals(localVariable.name)) {
                    final ASTNode parent = node.getParent();
                    if (parent instanceof MethodInvocation) {
                        registerMethodCallOnReceiver((MethodInvocation) parent);
                    } else if (parent instanceof Assignment) {
                        evaluateAssignment((Assignment) parent);
                    } else if (parent instanceof VariableDeclarationFragment) {
                        evaluateVariableDeclarationFragment((VariableDeclarationFragment) parent);
                    }
                }
                return true;
            }

            private void evaluateVariableDeclarationFragment(final VariableDeclarationFragment fragment) {
                final Expression initializer = fragment.getInitializer();
                if (initializer instanceof ClassInstanceCreation) {
                    registerConstructorCallOnVariable((ClassInstanceCreation) initializer);
                }
            }

            private void evaluateAssignment(final Assignment a) {
                final Expression rhs = a.getRightHandSide();
                if (rhs instanceof ClassInstanceCreation) {
                    registerConstructorCallOnVariable((ClassInstanceCreation) rhs);
                }
            }

            private void registerConstructorCallOnVariable(final ClassInstanceCreation invoke) {
                final IMethodBinding b = invoke.resolveConstructorBinding();
                final IMethodName method = BindingUtils.toMethodName(b);
                if (method != null) {
                    receiverMethodInvocations.add(method);
                }
            }

            private void registerMethodCallOnReceiver(final MethodInvocation invoke) {
                final IMethodBinding b = invoke.resolveMethodBinding();
                final IMethodName method = BindingUtils.toMethodName(b);
                if (method != null) {
                    receiverMethodInvocations.add(method);
                }
            }
        });
        return true;
    }

    @Override
    public Set<IMethodName> getReceiverMethodInvocations() {
        return receiverMethodInvocations;
    }
}
