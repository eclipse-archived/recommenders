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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class ProposalMatcher {

    private final String jdtMethodName;
    private Pattern[] jdtParameterTypePatterns;
    private final String[] jdtGenericTypeArguments;

    public ProposalMatcher(final CompletionProposal proposal) {
        ensureIsNotNull(proposal);
        jdtMethodName = String.valueOf(proposal.getName());
        final String jdtVariableTypeDeclarationSignature = String.valueOf(proposal.getDeclarationSignature());
        jdtGenericTypeArguments = Signature.getTypeArguments(jdtVariableTypeDeclarationSignature);
        final String jdtMethodSignature = String.valueOf(proposal.getSignature());
        initializeGenericTypeArgumentPatterns(jdtMethodSignature);
    }

    private void initializeGenericTypeArgumentPatterns(final String jdtMethodSignature) {
        final String[] jdtParameterTypes = Signature.getParameterTypes(jdtMethodSignature);
        jdtParameterTypePatterns = new Pattern[jdtParameterTypes.length];
        for (int i = 0; i < jdtParameterTypes.length; i++) {
            for (final String genericTypeArgument : jdtGenericTypeArguments) {
                // fuzzy match to get List<T> matches too;
                jdtParameterTypes[i] = StringUtils.replace(jdtParameterTypes[i], genericTypeArgument, ".*");
            }
            jdtParameterTypePatterns[i] = Pattern.compile(jdtParameterTypes[i]);
        }
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
        return crParameterTypes.length != jdtParameterTypePatterns.length;
    }

    private boolean hasSameParameterTypes(final ITypeName[] crParameterTypes) {
        for (int i = 0; i < crParameterTypes.length; i++) {
            final String srcTypeName = Names.vm2srcQualifiedType(crParameterTypes[i]);
            if (!jdtParameterTypePatterns[i].matcher(srcTypeName).matches()) {
                return false;
            }
        }
        return true;
    }
}