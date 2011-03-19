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

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.RequestType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;
import org.eclipse.recommenders.rcp.utils.ast.TypeDeclarationInformationAnalyzer;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SimilarMethodsSearchRequestCreator extends ASTVisitor {

    private final Request request = Request.createEmptyRequest(RequestType.SIMILAR_METHODS);

    public SimilarMethodsSearchRequestCreator(final MethodDeclaration methodDeclaration) {
        super(false);
        ensureIsNotNull(methodDeclaration, "not ast");
        methodDeclaration.accept(this);
        addExtendsAndImplementsTerms(methodDeclaration);
    }

    private void addExtendsAndImplementsTerms(final MethodDeclaration methodDeclaration) {
        final TypeDeclarationInformationAnalyzer t = new TypeDeclarationInformationAnalyzer(methodDeclaration);

        if (t.foundSuperclass()) {
            request.query.extendedTypes = Collections.singleton(t.getSuperclass());
        }
        request.query.implementedTypes = Sets.newHashSet(t.getSuperInterfaces());
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
        return false;
    }

    @Override
    public boolean visit(final MethodDeclaration node) {
        final IMethodBinding binding = node.resolveBinding();
        setOverridenIMethodNameIfAny(binding);
        return true;
    }

    private void setOverridenIMethodNameIfAny(final IMethodBinding b) {
        final IMethodBinding overriddenBinding = Bindings.findOverriddenMethod(b, true);
        final IMethodName overriddenIMethodName = BindingUtils.toMethodName(overriddenBinding);
        if (overriddenIMethodName != null) {
            request.query.overriddenMethods.add(overriddenIMethodName);
        }
    }

    // ========================= Method Level Instructions hacking
    @Override
    public boolean visit(final SimpleType node) {
        final ITypeBinding b = node.resolveBinding();
        addUsedType(b);
        return true;
    }

    @Override
    public boolean visit(final QualifiedType node) {
        final ITypeBinding b = node.resolveBinding();
        addUsedType(b);
        return true;
    }

    @Override
    public boolean visit(final ClassInstanceCreation node) {
        final IMethodBinding b = node.resolveConstructorBinding();
        addUsedMethod(b);
        return true;
    }

    @Override
    public boolean visit(final SuperConstructorInvocation node) {
        final IMethodBinding b = node.resolveConstructorBinding();
        addUsedMethod(b);
        return true;
    }

    @Override
    public boolean visit(final ConstructorInvocation node) {
        final IMethodBinding b = node.resolveConstructorBinding();
        addUsedMethod(b);
        return true;
    }

    @Override
    public boolean visit(final SuperMethodInvocation node) {
        final IMethodBinding b = node.resolveMethodBinding();
        addUsedMethod(b);
        return true;
    }

    @Override
    public boolean visit(final MethodInvocation node) {
        final IMethodBinding b = node.resolveMethodBinding();
        addUsedMethod(b);
        return true;
    }

    @Override
    public boolean visit(final SimpleName node) {
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
            // XXX Marcel: no need for those "eager" query enrichments. I tend
            // to think that this is too eager
            // addMethodParametersToUses(method);
            // addMethodReturnTypeToUses(method);
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

    private boolean isPrimitiveOrArrayOrNullOrObjectOrString(final ITypeName type) {
        return type == null || type.isPrimitiveType() || type.isArrayType() || type == VmTypeName.OBJECT
                || type == VmTypeName.STRING;
    }

}
