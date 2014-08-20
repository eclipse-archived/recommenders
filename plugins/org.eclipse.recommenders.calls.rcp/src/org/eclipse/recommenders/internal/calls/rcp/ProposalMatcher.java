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

import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public class ProposalMatcher {

    private final String proposedName;
    private final ITypeName[] proposedParameterTypes;

    public ProposalMatcher(IMethodName proposedMethod) {
        proposedName = proposedMethod.getName();
        proposedParameterTypes = proposedMethod.getParameterTypes();
    }

    public boolean match(@Nullable IMethodName candidate) {
        if (candidate == null) {
            return false;
        }

        String candidateName = candidate.getName();
        if (!candidateName.equals(proposedName)) {
            return false;
        }

        ITypeName[] candidateParameterTypes = candidate.getParameterTypes();
        if (proposedParameterTypes == null || candidateParameterTypes.length != proposedParameterTypes.length) {
            return false;
        }
        for (int i = candidateParameterTypes.length; i-- > 0;) {
            if (!candidateParameterTypes[i].equals(proposedParameterTypes[i])) {
                return false;
            }
        }

        return true;
    }
}
