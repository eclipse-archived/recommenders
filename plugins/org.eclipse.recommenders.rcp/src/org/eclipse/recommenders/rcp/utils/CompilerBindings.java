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
package org.eclipse.recommenders.rcp.utils;

import static com.google.common.base.Optional.absent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class CompilerBindings {

    /**
     * TODO nested anonymous types are not resolved correctly. JDT uses line numbers for inner types instead of $1,..,$n
     */
    public static Optional<ITypeName> toTypeName(@Nullable TypeBinding binding) {
        if (binding == null) {
            return absent();
        }

        try {
            binding = binding.original();

            char[] signature = binding.signature();
            if (signature == null) {
                return absent();
            }

            final ITypeName result;
            if (isPrimitiveBaseType(binding)) {
                result = VmTypeName.get(String.valueOf(signature));
            } else {
                // Strip of semicolon.
                result = VmTypeName.get(String.valueOf(signature, 0, signature.length - 1));
            }

            return Optional.of(result);
        } catch (final RuntimeException e) {
            return absent();
        }
    }

    private static boolean isPrimitiveBaseType(TypeBinding binding) {
        if (binding.isArrayType()) {
            return ((ArrayBinding) binding).elementsType().isBaseType();
        } else {
            return binding.isBaseType();
        }
    }

    public static Optional<IMethodName> toMethodName(@Nullable MethodBinding binding) {
        if (binding == null) {
            return absent();
        }

        try {
            binding = binding.original();
            ITypeName declaringType = toTypeName(binding.declaringClass).orNull();
            if (declaringType == null) {
                return absent();
            }
            char[] name = binding.selector;
            if (name == null) {
                return absent();
            }
            char[] signature = binding.signature();
            if (signature == null) {
                return absent();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(declaringType.getIdentifier()).append('.');
            sb.append(name);
            sb.append(signature);
            IMethodName result = VmMethodName.get(sb.toString());
            return Optional.of(result);
        } catch (final RuntimeException e) {
            return absent();
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static final ASTNode UNKNOWN = new UNKNOWN();

    private static final class UNKNOWN extends ASTNode {

        @Override
        public StringBuffer print(int indent, StringBuffer output) {
            output.append("UNKNOWN");
            return output;
        }
    }
}
