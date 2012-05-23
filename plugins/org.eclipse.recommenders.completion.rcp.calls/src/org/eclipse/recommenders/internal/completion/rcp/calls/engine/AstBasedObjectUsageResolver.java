/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.engine;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.rcp.ast.BindingUtils.getVariableBinding;
import static org.eclipse.recommenders.utils.rcp.ast.BindingUtils.toMethodName;
import static org.eclipse.recommenders.utils.rcp.ast.BindingUtils.toTypeName;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
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
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.rcp.ast.BindingUtils;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class AstBasedObjectUsageResolver extends ASTVisitor {

    private String varname;
    private ObjectUsage res;

    @Override
    public boolean visit(final MethodInvocation node) {
        if (receiverExpressionMatchesVarname(node.getExpression()) || (isThis() && isReceiverThis(node))) {
            final IMethodBinding b = node.resolveMethodBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        return false;
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
            return isThis();
        default:
            return false;
        }
    }

    private boolean matchesVarName(@Nullable final Name node) {
        if (node == null) {
            return false;
        }
        final String name = node.getFullyQualifiedName();
        return varname.equals(name);
    }

    private boolean isThis() {
        return "this".equals(varname);
    }

    private boolean isReceiverThis(final MethodInvocation mi) {
        final Expression expression = mi.getExpression();
        // standard case:
        if ((expression == null) && !isStatic(mi)) {
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
        final Optional<IMethodName> opt = BindingUtils.toMethodName(b);
        if (opt.isPresent()) {
            res.calls.add(opt.get());
        }
    }

    public ObjectUsage findObjectUsage(final String varname, final MethodDeclaration method) {
        ensureIsNotNull(varname);
        ensureIsNotNull(method);
        setVarname(varname);
        initializeResult();
        if ("".equals(varname) || "this".equals(varname) || "super".equals(varname)) {
            res.kind = Kind.THIS;
        }
        method.accept(this);

        if (res.kind == null)
            res.kind = Kind.FIELD;
        return res;
    }

    private void setVarname(final String varname) {
        this.varname = varname;
    }

    private void initializeResult() {
        res = new ObjectUsage();
    }

    @Override
    public boolean visit(final Assignment node) {

        final Expression lhs = node.getLeftHandSide();
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
        final Expression rhs = node.getRightHandSide();
        evaluateRightHandSideExpression(rhs);
        return true;
    }

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
            res.definition = toMethodName(mi.resolveMethodBinding()).orNull();
            res.kind = Kind.METHOD_RETURN;
            break;
        case ASTNode.SUPER_METHOD_INVOCATION:
            // x = super.some()
            final SuperMethodInvocation smi = cast(expression);
            res.definition = toMethodName(smi.resolveMethodBinding()).orNull();
            res.kind = Kind.METHOD_RETURN;
            break;
        case ASTNode.CLASS_INSTANCE_CREATION:
            final ClassInstanceCreation cic = cast(expression);
            res.definition = toMethodName(cic.resolveConstructorBinding()).orNull();
            res.kind = Kind.NEW;
            break;
        case ASTNode.SIMPLE_NAME:
            // e.g. int j=anotherValue;
            final SimpleName sn = cast(expression);
            // some alias thing...
            // it might be that we found an assignment before and this simpleName is just "$missing". Then ignore this
            if (res.kind == null)
                res.kind = Kind.UNKNOWN;
            break;
        default:
            break;
        }
    }

    // calls like 'this(args)'
    @Override
    public boolean visit(final ConstructorInvocation node) {
        if (isThis()) {
            final IMethodBinding b = node.resolveConstructorBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SingleVariableDeclaration node) {
        // declaration of parameters:
        if (matchesVarName(node.getName())) {
            final IVariableBinding b = node.resolveBinding();
            evaluateVariableBinding(b);
        }
        return true;
    }

    private void evaluateVariableBinding(final IVariableBinding b) {
        if ((b == null) || !matchesVarname(b) || isVartypeKnown()) {
            return;
        }
        res.type = toTypeName(b.getType()).orNull();
        if (res.kind != null) {
            // warning?
            return;
        }
        if (b.isParameter()) {
            res.kind = Kind.PARAMETER;
            final IMethodName method = BindingUtils.toMethodName(b.getDeclaringMethod()).orNull();
            if (method != null) {
                res.definition = method;
            }
        } else if (b.isField()) {
            res.kind = Kind.FIELD;
        } else {
            res.kind = Kind.UNKNOWN;
        }
    }

    private boolean matchesVarname(final IVariableBinding b) {
        return varname.equals(b.getName());
    }

    private boolean isVartypeKnown() {
        final boolean isVarTypeKnown = res.type != null;
        return isVarTypeKnown;
    }

    @Override
    public boolean visit(final SuperConstructorInvocation node) {
        if (isThis()) {
            final IMethodBinding b = node.resolveConstructorBinding();
            registerMethodCallOnReceiver(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SuperMethodInvocation node) {
        if (isThis()) {
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

    @Override
    public boolean visit(VariableDeclarationExpression node) {
        for (VariableDeclarationFragment f : (List<VariableDeclarationFragment>) node.fragments()) {
            evaluateVariableDeclarationFragment(f);
        }
        return true;
    }

    @Override
    public boolean visit(final QualifiedName node) {
        evaluateName(node);
        return true;
    }

    private void evaluateName(final Name node) {
        // name is almost every literal in the file. check for literals that look like the variable name we are looking
        // for:
        // if found initialize the type and definition kind of the variable once.
        if (matchesVarName(node)) {
            evaluateVariableBinding(getVariableBinding(node));
        }
    }

    @Override
    public boolean visit(final SimpleName node) {
        evaluateName(node);
        return true;
    }

    private Optional<IVariableBinding> findReceiver(final MethodInvocation call) {
        final Expression exp = call.getExpression();
        if ((exp == null) && !isStatic(call)) {
            // might be this! but not necessarily! --> static imports!
            return of((IVariableBinding) new ThisVariableBinding());
        }
        switch (exp.getNodeType()) {
        case ASTNode.SIMPLE_NAME:
        case ASTNode.QUALIFIED_NAME:
            final Name name = cast(exp);
            final IVariableBinding b = BindingUtils.getVariableBinding(name);
            return fromNullable(b);
        case ASTNode.THIS_EXPRESSION:
            return of((IVariableBinding) new ThisVariableBinding());
        }
        return absent();
    }

    // private void evaluateDefinitionByAssignment(final ASTNode node) {
    //
    // } else if (node instanceof ParenthesizedExpression) {
    // final ParenthesizedExpression pExp = (ParenthesizedExpression) node;
    // evaluateDefinitionByAssignment(pExp.getExpression());
    // } else if (node instanceof ConditionalExpression) {
    // final ConditionalExpression cond = (ConditionalExpression) node;
    // evaluateDefinitionByAssignment(cond.getThenExpression());
    // evaluateDefinitionByAssignment(cond.getElseExpression());
    // }
    // }

    private class ThisVariableBinding implements IVariableBinding {

        @Override
        public IAnnotationBinding[] getAnnotations() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getKind() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getModifiers() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isDeprecated() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRecovered() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSynthetic() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public IJavaElement getJavaElement() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getKey() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isEqualTo(final IBinding binding) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isField() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEnumConstant() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isParameter() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public String getName() {
            return "this";
        }

        @Override
        public ITypeBinding getDeclaringClass() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ITypeBinding getType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getVariableId() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getConstantValue() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IMethodBinding getDeclaringMethod() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IVariableBinding getVariableDeclaration() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}