/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class ProposalMatcher {

    private static final String[] NO_TYPE_PARAMETERS = new String[0];

    private final ITypeName[] jParams;
    private final String jName;

    private static Field fOriginalSignature;

    static {
        // workaround needed to handle proposals with generics properly.
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=380203
        try {
            fOriginalSignature = InternalCompletionProposal.class.getDeclaredField("originalSignature"); //$NON-NLS-1$
            fOriginalSignature.setAccessible(true);
        } catch (Exception e) {
        }
    }

    private static boolean canUseReflection(CompletionProposal proposal) {
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=380203
        return proposal instanceof InternalCompletionProposal && fOriginalSignature != null
                && fOriginalSignature.isAccessible();
    }

    public ProposalMatcher(CompletionProposal proposal, Optional<TypeBinding> receiverTypeBinding) {
        final String jSignature = getSignature(proposal);
        final String[] parameterTypes = Signature.getParameterTypes(jSignature);
        final String[] methodTypeParameters = Signature.getTypeParameters(jSignature);

        jName = String.valueOf(proposal.getName());
        jParams = new ITypeName[parameterTypes.length];

        final String[] classTypeParameters = extractClassTypeParameters(receiverTypeBinding);

        for (int i = 0; i < jParams.length; i++) {
            jParams[i] = asTypeName(parameterTypes[i], methodTypeParameters, classTypeParameters);
        }
    }

    private String[] extractClassTypeParameters(Optional<TypeBinding> receiverTypeBinding) {
        if (!receiverTypeBinding.isPresent()) {
            return NO_TYPE_PARAMETERS;
        }
        TypeBinding typeBinding = receiverTypeBinding.get();
        if (!typeBinding.isParameterizedTypeWithActualArguments() || !(typeBinding instanceof ParameterizedTypeBinding)) {
            return NO_TYPE_PARAMETERS;
        }
        ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) typeBinding;
        TypeVariableBinding[] typeVariableBindings = parameterizedTypeBinding.genericType().typeVariables();
        final String[] classTypeParameters = new String[typeVariableBindings.length];
        for (int i = 0; i < classTypeParameters.length; i++) {
            TypeVariableBinding typeVariableBinding = typeVariableBindings[i];
            classTypeParameters[i] = String.valueOf(typeVariableBinding.genericSignature());
        }
        return classTypeParameters;
    }

    private ITypeName asTypeName(String typeSignature, String[] primaryTypeParameters, String[] secondaryTypeParameters) {
        int signatureKind = Signature.getTypeSignatureKind(typeSignature);
        switch (signatureKind) {
        case Signature.ARRAY_TYPE_SIGNATURE:
            int arrayCount = Signature.getArrayCount(typeSignature);
            return VmTypeName.get(StringUtils.repeat('[', arrayCount)
                    + asTypeName(Signature.getElementType(typeSignature), primaryTypeParameters,
                            secondaryTypeParameters).getIdentifier());
        case Signature.CLASS_TYPE_SIGNATURE:
            final String erasedTypedSignature = Signature.getTypeErasure(typeSignature);
            return VmTypeName.get(StringUtils.removeEnd(erasedTypedSignature.replace('.', '/'), ";"));
        case Signature.BASE_TYPE_SIGNATURE:
            return VmTypeName.get(typeSignature);
        case Signature.TYPE_VARIABLE_SIGNATURE:
            String identifier = StringUtils.substring(typeSignature, 1, typeSignature.length() - 1);
            ITypeName typeParameter = locateTypeParameter(identifier, primaryTypeParameters, secondaryTypeParameters);
            if (typeParameter == null) {
                typeParameter = locateTypeParameter(identifier, secondaryTypeParameters, NO_TYPE_PARAMETERS);
            }
            return typeParameter;
        case Signature.WILDCARD_TYPE_SIGNATURE:
            return null; // Could not think of a case where this occurs.
        case Signature.CAPTURE_TYPE_SIGNATURE:
            return null; // Could not think of a case where this occurs.
        case Signature.INTERSECTION_TYPE_SIGNATURE:
            return null; // Could not think of a case where this occurs.
        default:
            return null; // Unknown/unsupported type signature.
        }
    }

    /**
     * Locates the type of the type variable {@code identifier} in {@code primaryTypeParameters}.
     *
     * Primary type parameters (e.g., of a method) can reference secondary type parameters (of a class) but not vice
     * versa.
     */
    private ITypeName locateTypeParameter(String identifier, String[] primaryTypeParameters,
            String[] secondaryTypeParameters) {
        for (int i = 0; i < primaryTypeParameters.length; i++) {
            final String primaryTypeParameter = primaryTypeParameters[i];
            if (primaryTypeParameter == null) {
                continue;
            }
            String typeVariable = Signature.getTypeVariable(primaryTypeParameter);
            if (typeVariable.equals(identifier)) {
                String[] typeParameterBounds = Signature.getTypeParameterBounds(primaryTypeParameter);
                if (typeParameterBounds.length == 0) {
                    return VmTypeName.OBJECT;
                } else {
                    return asTypeName(typeParameterBounds[0], primaryTypeParameters, secondaryTypeParameters);
                }
            }
        }
        return null;
    }

    private String getSignature(CompletionProposal proposal) {
        try {
            if (canUseReflection(proposal)) {
                char[] s = (char[]) fOriginalSignature.get(proposal);
                if (s != null) {
                    return String.valueOf(s);
                }
            }
        } catch (Exception e) {
            // catch whatever goes wrong and return fall back instead
        }
        return String.valueOf(proposal.getSignature());
    }

    public boolean match(IMethodName rMethod) {
        String rName = rMethod.getName();
        ITypeName[] rParams = rMethod.getParameterTypes();

        if (!rName.equals(jName)) {
            return false;
        }

        if (rParams.length != jParams.length) {
            return false;
        }

        for (int i = rParams.length; i-- > 0;) {
            if (!rParams[i].equals(jParams[i])) {
                return false;
            }
        }
        return true;
    }
}
