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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.recommenders.rcp.utils.MatchingUtils;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

@SuppressWarnings({ "restriction" })
public class ProposalMatcher {

    private final Optional<IMethodName> proposedMethod;

    public ProposalMatcher(CompletionProposal proposal, Optional<TypeBinding> receiverTypeBinding) {
        proposedMethod = Optional.fromNullable(MatchingUtils.asMethodName(proposal, receiverTypeBinding));
    }

    public boolean match(IMethodName candidate) {
        IMethodName method = proposedMethod.orNull();
        if (method == null) {
            return false;
        }

        String candidateName = candidate.getName();
        if (!candidateName.equals(method.getName())) {
            return false;
        }

        ITypeName[] params = method.getParameterTypes();
        ITypeName[] candidateParams = candidate.getParameterTypes();
        if (candidateParams.length != params.length) {
            return false;
        }
        for (int i = candidateParams.length; i-- > 0;) {
            if (!candidateParams[i].equals(params[i])) {
                return false;
            }
        }

        return true;
    }
}
