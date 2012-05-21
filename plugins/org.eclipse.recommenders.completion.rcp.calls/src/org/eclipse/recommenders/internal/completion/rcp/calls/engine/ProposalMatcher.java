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
package org.eclipse.recommenders.internal.completion.rcp.calls.engine;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public class ProposalMatcher {

    private String jSignature;
    private String[] jParams;
    private String jName;
    private String rName;
    private ITypeName[] rParams;

    public ProposalMatcher(CompletionProposal proposal) {
        jSignature = String.valueOf(proposal.getSignature());
        System.out.println(jSignature);
        jName = String.valueOf(proposal.getName());
        jParams = Signature.getParameterTypes(jSignature);

        for (int i = 0; i < jParams.length; i++) {
            String param = Signature.getTypeErasure(jParams[i]);
            param = param.replace('.', '/');
            param = StringUtils.removeEnd(param, ";");
            if ("!*".equals(param)) {
                param = "Ljava/lang/Object";
                // XXX that TT stuff must be solved better... forum thread started on this.
            } else if (param.endsWith("TT")) {
                param = param.replace("TT", "Ljava/lang/Object");
            }
            jParams[i] = param;
        }
    }

    public boolean match(IMethodName rMethod) {
        rName = rMethod.getName();
        rParams = rMethod.getParameterTypes();

        if (!sameName()) {
            return false;
        }
        if (!sameNumberOfParameters()) {
            return false;
        }

        for (int i = jParams.length; i-- > 0;) {

            if (!sameParameterType(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean sameParameterType(int i) {
        return jParams[i].equals(rParams[i].getIdentifier());
    }

    private boolean sameNumberOfParameters() {
        return jParams.length == rParams.length;
    }

    private boolean sameName() {
        return jName.equals(rName);
    }
}
