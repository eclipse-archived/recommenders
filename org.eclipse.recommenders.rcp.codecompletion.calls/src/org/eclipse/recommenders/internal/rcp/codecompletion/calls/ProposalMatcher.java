/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class ProposalMatcher {

    private final String jdtMethodName;
    private final String[] jdtParameterTypes;
    private final String[] jdtGenericTypeArguments;

    public ProposalMatcher(final CompletionProposal proposal) {
        ensureIsNotNull(proposal);
        jdtMethodName = String.valueOf(proposal.getName());
        final String jdtMethodSignature = String.valueOf(proposal.getSignature());
        final String jdtVariableTypeDeclarationSignature = String.valueOf(proposal.getDeclarationSignature());
        jdtParameterTypes = Signature.getParameterTypes(jdtMethodSignature);
        jdtGenericTypeArguments = Signature.getTypeArguments(jdtVariableTypeDeclarationSignature);
    }

    public boolean matches(final IMethodName crMethod) {

        if (!hasSameMethodName(crMethod)) {
            return false;
        }
        final ITypeName[] crParameterTypes = crMethod.getParameterTypes();
        if (hasSameParameterCount(crParameterTypes)) {
            return false;
        }
        return hasSameParameterTypes(crParameterTypes);
    }

    private boolean hasSameMethodName(final IMethodName crMethod) {
        final String crMethodName = crMethod.getName();
        return crMethodName.equals(jdtMethodName);
    }

    private boolean hasSameParameterCount(final ITypeName[] crParameterTypes) {
        return crParameterTypes.length != jdtParameterTypes.length;
    }

    private boolean hasSameParameterTypes(final ITypeName[] crParameterTypes) {
        for (int i = 0; i < crParameterTypes.length; i++) {
            if (isArgumentTypeAPotentialGenericReplacement(jdtParameterTypes[i])) {
                continue;
            }
            // if I'm here than the parameter type MUST match
            if (!isSameTypeIdentifier(crParameterTypes[i], jdtParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isArgumentTypeAPotentialGenericReplacement(final String jdtParameterType) {
        for (final String genericTypeArgument : jdtGenericTypeArguments) {
            // fuzzy match to get List<T> matches too;
            if (jdtParameterType.contains(genericTypeArgument)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameTypeIdentifier(final ITypeName crParameterType, final String jdtParameterType) {
        return jdtParameterType.equals(crParameterType.getIdentifier().replace('/', '.'));
    }

}