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
package org.eclipse.recommenders.internal.calls.rcp;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.*;
import static org.eclipse.recommenders.rcp.utils.AstBindings.toMethodName;
import static org.eclipse.recommenders.utils.Checks.*;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.rcp.utils.AstBindings;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class AstDefUseFinder extends ASTVisitor {

    private IMethodName definingMethod;
    private DefinitionKind defKind = UNKNOWN;
    private final List<IMethodName> calls = Lists.newLinkedList();
    private final MethodDeclaration method;
    private final String varname;

    public AstDefUseFinder(final String varname, MethodDeclaration method) {
        this.varname = ensureIsNotNull(varname);
        this.method = ensureIsNotNull(method);

        if ("this".equals(varname) || "super".equals(varname)) {
            defKind = THIS;
        }
        method.accept(this);
    }

    public List<IMethodName> getCalls() {
        return calls;
    }

    public Optional<IMethodName> getDefiningMethod() {
        return fromNullable(definingMethod);
    }

    public DefinitionKind getDefinitionKind() {
        return defKind;
    }

    @Override
    public boolean visit(final MethodInvocation node) {
        Expression expr = node.getExpression();
        if (receiverExpressionMatchesVarname(expr) || maybeThis() && isReceiverThis(node)) {
            final IMethodBinding b = node.resolveMethodBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    private boolean receiverExpressionMatchesVarname(@Nullable final Expression exp) {
        if (exp == null) {
            return false;
        }
        switch (exp.getNodeType()) {
        case ASTNode.SIMPLE_NAME:
        case ASTNode.QUALIFIED_NAME:
            final Name name = cast(exp);
            // is it the same name we are looking for?
            return matchesVarName(name);
        case ASTNode.THIS_EXPRESSION:
            // do we look for this?
            return maybeThis();
        default:
            return false;
        }
    }

    private boolean matchesVarName(@Nullable final Name node) {
        if (node == null) {
            return false;
        }
        final String name = node.getFullyQualifiedName();
        boolean equals = varname.equals(name);
        if (equals && defKind == UNKNOWN) {
            refineDefKindByBinding(node);
        }
        return equals;
    }

    private void refineDefKindByBinding(final Name node) {
        IVariableBinding b = Checks.castOrNull(node.resolveBinding());
        if (b == null) {
            return;
        } else if (b.isField()) {
            defKind = FIELD;
        } else if (b.isParameter()) {
            defKind = PARAM;
        }
    }

    private boolean maybeThis() {
        return "this".equals(varname) || "".equals(varname) || "super".equals(varname);
    }

    private boolean isReceiverThis(final MethodInvocation mi) {
        final Expression expression = mi.getExpression();
        // standard case:
        if (expression == null && !isStatic(mi)) {
            return true;
        }
        // qualified call: this.method()
        if (expression instanceof ThisExpression) {
            return true;
        }
        return false;
    }

    private boolean isStatic(final MethodInvocation call) {
        final IMethodBinding binding = call.resolveMethodBinding();
        if (binding != null) {
            return JdtFlags.isStatic(binding);
        }
        // let's assume it's not static...
        return false;
    }

    private void registerMethodCallOnReceiver(final IMethodBinding b) {
        final Optional<IMethodName> opt = AstBindings.toMethodName(b);
        if (opt.isPresent()) {
            calls.add(opt.get());
        }
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(final Assignment node) {

        final Expression lhs = node.getLeftHandSide();
        final Expression rhs = node.getRightHandSide();
        if (isCompletionNode(rhs)) {
            switch (lhs.getNodeType()) {
            case ASTNode.SIMPLE_NAME:
            case ASTNode.QUALIFIED_NAME:
                // callee is explicit (as in o.$). we don't have to go further
                break;
            case ASTNode.THIS_EXPRESSION:
                defKind = THIS;
                break;
            case ASTNode.STRING_LITERAL:
                defKind = STRING_LITERAL;
                break;
            case ASTNode.ARRAY_ACCESS:
                defKind = ARRAY_ACCESS;
                break;
            case ASTNode.FIELD_ACCESS:
                FieldAccess f = (FieldAccess) lhs;
                Expression e = f.getExpression();
                switch (e.getNodeType()) {
                case ASTNode.THIS_EXPRESSION:
                    defKind = THIS;
                    break;
                case ASTNode.ARRAY_ACCESS:
                    defKind = ARRAY_ACCESS;
                    break;
                default:
                    // when we have completely broken code, this may happen... ignore it.
                    // throwUnreachable(
                    // "Did not expect this LHS expression to be possible here. Pls report this snippet: %s", lhs);
                }
                break;
            default:
                // when we have completely broken code, this may happen... ignore it.
                // throwUnreachable("Did not expect this LHS expression to be possible here. Pls report this snippet: %s",
                // lhs);
            }
        }

        if (lhs == null) {
            return true;
        }
        switch (lhs.getNodeType()) {
        case ASTNode.SIMPLE_NAME:
        case ASTNode.QUALIFIED_NAME:
            final Name n = cast(lhs);
            if (!matchesVarName(n)) {
                return true;
            }
            break;
        default:
            return true;
        }
        evaluateRightHandSideExpression(rhs);
        return true;
    }

    private boolean isCompletionNode(final Expression rhs) {
        return rhs instanceof SimpleName && ((SimpleName) rhs).getIdentifier().equals("$missing$");
    }

    // called only if left hand side was a match.
    private void evaluateRightHandSideExpression(final Expression expression) {
        switch (expression.getNodeType()) {
        case ASTNode.CAST_EXPRESSION:
            final CastExpression ce = cast(expression);
            // re-evaluate using the next expression:
            evaluateRightHandSideExpression(ce.getExpression());
            break;
        case ASTNode.METHOD_INVOCATION:
            // x = some().method().call()
            final MethodInvocation mi = cast(expression);
            definingMethod = toMethodName(mi.resolveMethodBinding()).orNull();
            defKind = RETURN;
            break;
        case ASTNode.SUPER_METHOD_INVOCATION:
            // x = super.some()
            final SuperMethodInvocation smi = cast(expression);
            definingMethod = toMethodName(smi.resolveMethodBinding()).orNull();
            defKind = RETURN;
            break;
        case ASTNode.CLASS_INSTANCE_CREATION:
            final ClassInstanceCreation cic = cast(expression);
            definingMethod = toMethodName(cic.resolveConstructorBinding()).orNull();
            defKind = NEW;
            break;
        case ASTNode.ARRAY_ACCESS:
            defKind = ARRAY_ACCESS;
            break;
        case ASTNode.NULL_LITERAL:
            defKind = NULL_LITERAL;
            break;
        case ASTNode.SIMPLE_NAME:
            // e.g. int j=anotherValue;
            // some alias thing...
            // it might be that we found an assignment before and this simpleName is just "$missing". Then ignore this
            if (defKind == null) {
                defKind = LOCAL;
            }
            break;
        default:
            break;
        }
    }

    // calls like 'this(args)'
    @Override
    public boolean visit(final ConstructorInvocation node) {
        if (maybeThis()) {
            final IMethodBinding b = node.resolveConstructorBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SingleVariableDeclaration node) {
        if (matchesVarName(node.getName()) && node.getParent() instanceof MethodDeclaration) {
            defKind = PARAM;
            definingMethod = toMethodName(method.resolveBinding()).orNull();
        }
        return true;
    }

    @Override
    public boolean visit(final SuperConstructorInvocation node) {
        if (maybeThis()) {
            final IMethodBinding b = node.resolveConstructorBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SuperMethodInvocation node) {
        if (maybeThis()) {
            final IMethodBinding b = node.resolveMethodBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(final VariableDeclarationStatement node) {
        for (final VariableDeclarationFragment f : (List<VariableDeclarationFragment>) node.fragments()) {
            evaluateVariableDeclarationFragment(f);
        }
        return true;
    }

    private void evaluateVariableDeclarationFragment(final VariableDeclarationFragment f) {
        final SimpleName name = f.getName();
        if (matchesVarName(name)) {
            final Expression expression = f.getInitializer();
            if (expression != null) {
                evaluateRightHandSideExpression(expression);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(VariableDeclarationExpression node) {
        for (VariableDeclarationFragment f : (List<VariableDeclarationFragment>) node.fragments()) {
            evaluateVariableDeclarationFragment(f);
        }
        return true;
    }

}
