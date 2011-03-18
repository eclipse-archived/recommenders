/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch.completion;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.RequestType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;

@SuppressWarnings("restriction")
public class SearchRequestCreator extends ASTVisitor {
    private final ITextSelection selection;
    private final Request request = Request.createEmptyRequest();

    public SearchRequestCreator(final ASTNode ast, final ITextSelection selection) {
        super(false);
        ensureIsNotNull(ast, "not ast");
        ensureIsNotNull(selection, "not selection");
        this.selection = selection;
        determineRequestKind();
        ast.accept(this);
    }

    private void determineRequestKind() {
        final boolean empty = selection == null || selection.getLength() == 0;
        if (empty) {
            request.type = RequestType.SIMILAR_CLASSES;
        } else {
            request.type = RequestType.CUSTOM;
        }
    }

    public Request getRequest() {
        return request;
    }

    @Override
    public boolean visit(final ImportDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(final PackageDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(final TypeDeclaration node) {
        if (isNodeInSelectionRange(node)) {
            setSuperclassNames(node);
            setInterfaceNames(node);
        }
        return true;
    }

    private void setSuperclassNames(final TypeDeclaration node) {
        final ITypeBinding clazz = node.resolveBinding();
        ITypeBinding superclass = clazz.getSuperclass();
        // add superclass, if not null and not Object
        for (; superclass != null; superclass = superclass.getSuperclass()) {
            final ITypeName superclassName = BindingUtils.toTypeName(superclass);
            if (!isPrimitiveOrArrayOrNullOrObjectOrString(superclassName)) {
                request.query.extendedTypes.add(superclassName);
            }
        }
    }

    private void setInterfaceNames(final TypeDeclaration node) {
        for (final ITypeBinding interface_ : node.resolveBinding().getInterfaces()) {
            final ITypeName ITypeName = BindingUtils.toTypeName(interface_);
            if (!isPrimitiveOrArrayOrNullOrObjectOrString(ITypeName)) {
                request.query.implementedTypes.add(ITypeName);
            }
        }
    }

    @Override
    public boolean visit(final FieldDeclaration node) {
        if (isNodeInSelectionRange(node)) {
            final Type fieldType = node.getType();
            addField(fieldType);
        }
        return true;
    }

    private void addField(final Type fieldType) {
        final ITypeBinding fieldTypeBinding = fieldType.resolveBinding();
        final ITypeName ITypeName = BindingUtils.toTypeName(fieldTypeBinding);
        if (!isPrimitiveOrArrayOrNullOrObjectOrString(ITypeName)) {
            request.query.fieldTypes.add(ITypeName);
        }
    }

    @Override
    public boolean visit(final MethodDeclaration node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding binding = node.resolveBinding();
            setOverridenIMethodName(binding);
        }
        return true;
    }

    private void setOverridenIMethodName(final IMethodBinding b) {
        final IMethodBinding overriddenBinding = Bindings.findOverriddenMethod(b, true);
        final IMethodName overriddenIMethodName = BindingUtils.toMethodName(overriddenBinding);
        if (overriddenIMethodName != null) {
            request.query.overriddenMethods.add(overriddenIMethodName);
        }
    }

    // ========================= Method Level Instructions hacking
    @Override
    public boolean visit(final SimpleType node) {
        if (isNodeInSelectionRange(node)) {
            final ITypeBinding b = node.resolveBinding();
            addUsedType(b);
        }
        return true;
    }

    @Override
    public boolean visit(final QualifiedType node) {
        if (isNodeInSelectionRange(node)) {
            final ITypeBinding b = node.resolveBinding();
            addUsedType(b);
        }
        return true;
    }

    @Override
    public boolean visit(final ClassInstanceCreation node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding b = node.resolveConstructorBinding();
            addUsedMethod(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SuperConstructorInvocation node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding b = node.resolveConstructorBinding();
            addUsedMethod(b);
        }
        return true;
    }

    @Override
    public boolean visit(final ConstructorInvocation node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding b = node.resolveConstructorBinding();
            addUsedMethod(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SuperMethodInvocation node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding b = node.resolveMethodBinding();
            addUsedMethod(b);
        }
        return true;
    }

    @Override
    public boolean visit(final MethodInvocation node) {
        if (isNodeInSelectionRange(node)) {
            final IMethodBinding b = node.resolveMethodBinding();
            addUsedMethod(b);
        }
        return true;
    }

    @Override
    public boolean visit(final SimpleName node) {
        if (!isNodeInSelectionRange(node)) {
            return true;
        }
        final IBinding b = node.resolveBinding();
        if (b instanceof ITypeBinding) {
            addUsedType((ITypeBinding) b);
        } else if (b instanceof IVariableBinding) {
            final IVariableBinding var = (IVariableBinding) b;
            addUsedType(var.getType());
        } else if (b instanceof IMethodBinding) {
            // covered by several other visit methods: constructor calls, super
            // invocations, method invocation and class
            // instance creation
            // addUsedMethod((IMethodBinding) b);
        } else {
            System.out.println("debug other type: " + (b == null ? null : b.getClass()));
        }
        return true;
    }

    private void addUsedType(final ITypeBinding b) {
        final ITypeName type = BindingUtils.toTypeName(b);
        if (type != null && !isPrimitiveOrArrayOrNullOrObjectOrString(type)) {
            request.query.usedTypes.add(type);
        }
    }

    private void addUsedMethod(final IMethodBinding b) {
        final IMethodName method = BindingUtils.toMethodName(b);
        if (method != null) {
            request.query.calledMethods.add(method);
            addMethodParametersToUses(method);
            addMethodReturnTypeToUses(method);
        }
    }

    private void addMethodParametersToUses(final IMethodName IMethodName) {
        for (final ITypeName param : IMethodName.getParameterTypes()) {
            if (!isPrimitiveOrArrayOrNullOrObjectOrString(param)) {
                request.query.usedTypes.add(param);
            }
        }
    }

    private void addMethodReturnTypeToUses(final IMethodName IMethodName) {
        final ITypeName returnType = IMethodName.getReturnType();
        if (!isPrimitiveOrArrayOrNullOrObjectOrString(returnType)) {
            request.query.usedTypes.add(returnType);
        }
    }

    private boolean isNodeInSelectionRange(final ASTNode node) {
        if (!hasUserSelectedSomeText()) {
            return true;
        }
        final int nodeStart = node.getStartPosition();
        final int nodeEnd = nodeStart + node.getLength();
        final int selectionStart = selection.getOffset();
        final int selectionEnd = selectionStart + selection.getLength();
        final boolean nodeStartsAfterSelectionStart = nodeStart >= selectionStart;
        final boolean nodeEndsBeforeSelectionEnd = nodeEnd <= selectionEnd;
        return nodeStartsAfterSelectionStart && nodeEndsBeforeSelectionEnd;
    }

    private boolean hasUserSelectedSomeText() {
        return selection.getLength() > 0;
    }

    private boolean isPrimitiveOrArrayOrNullOrObjectOrString(final ITypeName type) {
        return type == null || type.isPrimitiveType() || type.isArrayType() || type == VmTypeName.OBJECT
                || type == VmTypeName.STRING;
    }
}
