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
package org.eclipse.recommenders.internal.rcp.codecompletion;

import static org.eclipse.recommenders.commons.utils.Option.none;
import static org.eclipse.recommenders.commons.utils.Option.wrap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

@SuppressWarnings("restriction")
public class CompilerBindings {

    /**
     * TODO nested anonymous types are not resolved correctly. JDT uses line
     * numbers for inner types instead of $1,..,$n
     */
    public static Option<ITypeName> toTypeName(@Nullable TypeBinding binding) {
        // XXX generics fail
        if (binding == null) {
            return none();
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
            final Option<ITypeName> typeName = toTypeName(leafComponentType);
            if (!typeName.hasValue()) {
                return none();
            }
            final VmTypeName res = VmTypeName.get(arrayDimensions + typeName.get().getIdentifier());
            return wrap(res);
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

        String signature = String.valueOf(binding.computeUniqueKey());
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
        final VmTypeName res = VmTypeName.get(signature);
        return wrap(res);
    }

    public static Option<IMethodName> toMethodName(final @Nullable MethodBinding binding) {
        if (binding == null) {
            return none();
        }
        final String uniqueKey = String.valueOf(binding.computeUniqueKey());
        final String qualifiedMethodName = StringUtils.substringBefore(uniqueKey, "(").replace(";.", ".");
        final String[] parameterTypes = Signature.getParameterTypes(uniqueKey);
        final String returnType = Signature.getReturnType(uniqueKey);
        final StringBuilder sb = new StringBuilder();
        sb.append(qualifiedMethodName).append("(");
        for (final String parameter : parameterTypes) {
            sb.append(parameter);
        }
        sb.append(")").append(returnType);
        final VmMethodName res = VmMethodName.get(sb.toString());
        return wrap(res);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static Option<ITypeName> toTypeName(final TypeReference type) {
        return (Option<ITypeName>) (type == null ? Option.none() : toTypeName(type.resolvedType));
    }
}
