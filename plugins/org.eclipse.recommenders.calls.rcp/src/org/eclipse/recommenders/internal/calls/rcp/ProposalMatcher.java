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

import static java.lang.String.valueOf;
import static org.eclipse.jdt.core.Signature.C_TYPE_VARIABLE;
import static org.eclipse.jdt.core.Signature.getArrayCount;
import static org.eclipse.jdt.core.Signature.getElementType;
import static org.eclipse.jdt.core.Signature.getParameterTypes;
import static org.eclipse.jdt.core.Signature.getTypeErasure;
import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

@SuppressWarnings("restriction")
public class ProposalMatcher {

    private String jSignature;
    private String[] jParams;
    private String jName;
    private String rName;
    private ITypeName[] rParams;

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

    public ProposalMatcher(CompletionProposal proposal) {
        jSignature = getSignature(proposal);
        jName = valueOf(proposal.getName());
        jParams = getParameterTypes(jSignature);

        for (int i = 0; i < jParams.length; i++) {
            String param = getTypeErasure(jParams[i]);
            String paramBaseType = getElementType(param);
            param = param.replace('.', '/');
            param = StringUtils.removeEnd(param, ";"); //$NON-NLS-1$
            if (isWildcardCapture(paramBaseType) || isTypeParameter(paramBaseType)) {
                int dimensions = getArrayCount(param);
                param = StringUtils.repeat('[', dimensions) + OBJECT.getIdentifier();
            }
            jParams[i] = param;
        }
    }

    private boolean isWildcardCapture(String param) {
        return param.charAt(0) == Signature.C_CAPTURE;
    }

    /**
     * @param param
     *            base type - no array dimensions are checked
     */
    private boolean isTypeParameter(String param) {
        return param.charAt(0) == C_TYPE_VARIABLE;
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
