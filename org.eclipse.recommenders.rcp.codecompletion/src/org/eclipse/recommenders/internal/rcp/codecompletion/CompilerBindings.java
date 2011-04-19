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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
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
    public static ITypeName toTypeName(@Nullable TypeBinding binding) {
        if (binding == null) {
            return null;
        }
        if (binding.isArrayType()) {
            final int dimensions = binding.dimensions();
            final TypeBinding leafComponentType = binding.leafComponentType();
            return VmTypeName.get(StringUtils.repeat("[", dimensions) + toTypeName(leafComponentType));
        }
        // TODO: handling of generics is bogus!
        if (binding instanceof TypeVariableBinding) {
            final TypeVariableBinding generic = (TypeVariableBinding) binding;
            binding = (TypeBinding) generic.declaringElement;

        }
        String signature = String.valueOf(binding.computeUniqueKey());
        if (signature.length() == 1) {
            return VmTypeName.get(signature);
        }

        if (signature.endsWith(";")) {
            signature = StringUtils.substringBeforeLast(signature, ";");
        } else {
            signature = "L" + SignatureUtil.stripSignatureToFQN(signature);
        }
        return VmTypeName.get(signature);
    }

    public static IMethodName toMethodName(final @Nullable MethodBinding binding) {
        if (binding == null) {
            return null;
        }
        toTypeName(binding.declaringClass);
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
        return VmMethodName.get(sb.toString());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static ITypeName toTypeName(final TypeReference type) {
        return type == null ? null : toTypeName(type.resolvedType);
    }
}
