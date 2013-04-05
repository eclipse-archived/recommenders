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
package org.eclipse.recommenders.utils.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class CompilerBindings {

    public static Optional<ITypeName> toTypeName(@Nullable Binding b) {
        if (b instanceof TypeBinding) {
            return toTypeName((TypeBinding) b);
        } else if (b instanceof VariableBinding) {
            TypeBinding type = ((VariableBinding) b).type;
            return toTypeName(type);
        } else
            return absent();
    }

    /**
     * TODO nested anonymous types are not resolved correctly. JDT uses line numbers for inner types instead of $1,..,$n
     */
    public static Optional<ITypeName> toTypeName(@Nullable TypeBinding binding) {
        // XXX generics fail
        if (binding == null) {
            return absent();
        }
        //
        final boolean boundParameterizedType = binding.isBoundParameterizedType();
        final boolean parameterizedType = binding.isParameterizedType();

        // if (binding.isBoundParameterizedType()) {
        // return null;
        // }

        if (binding.isArrayType()) {
            final int dimensions = binding.dimensions();
            final TypeBinding leafComponentType = binding.leafComponentType();
            final String arrayDimensions = StringUtils.repeat("[", dimensions);
            final Optional<ITypeName> typeName = toTypeName(leafComponentType);
            if (!typeName.isPresent()) {
                return absent();
            }
            final ITypeName res = VmTypeName.get(arrayDimensions + typeName.get().getIdentifier());
            return fromNullable(res);
        }
        // TODO: handling of generics is bogus!
        if (binding instanceof TypeVariableBinding) {
            final TypeVariableBinding generic = (TypeVariableBinding) binding;
            if (generic.declaringElement instanceof TypeBinding) {
                // XXX: for this?
                binding = (TypeBinding) generic.declaringElement;
            } else if (generic.superclass != null) {
                // example Tuple<T1 extends List, T2 extends Number) --> for
                // generic.superclass (T2)=Number
                // we replace the generic by its superclass
                binding = generic.superclass;
            }
        }

        String signature = String.valueOf(binding.genericTypeSignature());
        // if (binding instanceof BinaryTypeBinding) {
        // signature = StringUtils.substringBeforeLast(signature, ";");
        // }

        if (signature.length() == 1) {
            // no handling needed. primitives always look the same.
        } else if (signature.endsWith(";")) {
            signature = StringUtils.substringBeforeLast(signature, ";");
        } else {
            signature = "L" + SignatureUtil.stripSignatureToFQN(signature);
        }
        final ITypeName res = VmTypeName.get(signature);
        return fromNullable(res);
    }

    public static Optional<IMethodName> toMethodName(@Nullable final MethodBinding binding) {
        if (binding == null) {
            return absent();
        }
        try {
            final String uniqueKey = String.valueOf(binding.computeUniqueKey());
            String qualifiedMethodName = StringUtils.substringBefore(uniqueKey, "(").replace(";.", ".");
            if (qualifiedMethodName.endsWith("."))
                qualifiedMethodName += new String(TypeConstants.INIT);
            final String[] parameterTypes = Signature.getParameterTypes(uniqueKey);
            final String returnType = Signature.getReturnType(uniqueKey);
            final StringBuilder sb = new StringBuilder();
            sb.append(qualifiedMethodName).append("(");
            for (final String parameter : parameterTypes) {
                sb.append(parameter);
            }
            sb.append(")").append(returnType);
            final IMethodName res = VmMethodName.get(sb.toString());
            return of(res);
        } catch (final RuntimeException e) {
            // if the signature could not be parsed by JDT (because it is incomplete!):
            return absent();
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static Optional<ITypeName> toTypeName(final TypeReference type) {
        return (Optional<ITypeName>) (type == null ? Optional.absent() : toTypeName(type.resolvedType));
    }

    public static Optional<Binding> getBinding(final ASTNode node) {
        Binding b = null;
        if (node instanceof FieldDeclaration) {
            final FieldDeclaration f = (FieldDeclaration) node;
            b = f.binding;
        } else if (node instanceof MethodDeclaration) {
            final MethodDeclaration d = (MethodDeclaration) node;
            b = d.binding;
        } else if (node instanceof LocalDeclaration) {
            final LocalDeclaration l = (LocalDeclaration) node;
            b = l.binding;
        } else if (node instanceof TypeParameter) {
            final TypeParameter t = (TypeParameter) node;
            b = t.binding;
        }
        return fromNullable(b);
    }

    public static Optional<String> getVariableName(final Binding node) {
        if (node == null) {
            return absent();
        }
        switch (node.kind()) {
        case Binding.FIELD:
        case Binding.LOCAL:
            return of(String.valueOf(node.shortReadableName()));
        }
        return absent();
    }
}
