/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Andreas Sewe - better handling of generics.
 *    Johannes Dorn - refactoring.
 */
package org.eclipse.recommenders.completion.rcp.utils;

import static com.google.common.base.Optional.absent;
import static java.lang.Math.min;
import static org.eclipse.jdt.core.compiler.CharOperation.*;
import static org.eclipse.recommenders.rcp.utils.ReflectionUtils.getDeclaredField;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.recommenders.internal.rcp.LogMessages;
import org.eclipse.recommenders.rcp.utils.CompilerBindings;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

@SuppressWarnings("restriction")
public class ProposalUtils {

    private static final char[] INIT = "<init>".toCharArray();

    /**
     * Workaround needed to handle proposals with generic signatures properly.
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=380203"Bug 380203</a>.
     */
    private static Field ORIGINAL_SIGNATURE = getDeclaredField(InternalCompletionProposal.class, "originalSignature")
            .orNull();

    private static char[] getSignature(CompletionProposal proposal) {
        char[] signature = null;
        try {
            if (canUseReflection(proposal)) {
                signature = (char[]) ORIGINAL_SIGNATURE.get(proposal);
            }
        } catch (Exception e) {
            log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, ORIGINAL_SIGNATURE);
        }
        return signature != null ? signature : proposal.getSignature();
    }

    private static boolean canUseReflection(CompletionProposal proposal) {
        return proposal instanceof InternalCompletionProposal && ORIGINAL_SIGNATURE != null
                && ORIGINAL_SIGNATURE.isAccessible();
    }

    /**
     * @see <a href="https://www.eclipse.org/forums/index.php/m/1408138/">Forum discussion of the lookup strategy
     *      employed by this method</a>
     */
    public static Optional<IMethodName> toMethodName(CompletionProposal proposal, LookupEnvironment env) {
        Preconditions.checkArgument(isKindSupported(proposal));

        ReferenceBinding declaringType = getDeclaringType(proposal, env).orNull();
        if (declaringType == null) {
            return absent();
        }

        char[] methodName = proposal.isConstructor() ? INIT : proposal.getName();
        MethodBinding[] overloads = declaringType.getMethods(methodName);

        char[] proposalSignature = getSignature(proposal);
        char[] strippedProposalSignature = stripTypeParameters(proposalSignature);

        proposalSignature = replaceOnCopy(proposalSignature, '.', '/');
        strippedProposalSignature = replaceOnCopy(strippedProposalSignature, '.', '/');

        for (MethodBinding overload : overloads) {
            char[] signature = overload.genericSignature();
            if (signature == null) {
                signature = overload.signature();
            }
            if (Arrays.equals(proposalSignature, signature)) {
                return CompilerBindings.toMethodName(overload);
            }
            if (Arrays.equals(strippedProposalSignature, signature)) {
                return CompilerBindings.toMethodName(overload);
            }
        }

        return absent();
    }

    private static boolean isKindSupported(CompletionProposal proposal) {
        switch (proposal.getKind()) {
        case CompletionProposal.METHOD_REF:
            return true;
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            return true;
        case CompletionProposal.METHOD_DECLARATION:
            return true;
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
            return true;
        default:
            return false;
        }
    }

    private static char[] stripTypeParameters(char[] proposalSignature) {
        StringBuilder sb = new StringBuilder();

        // Copy optional type parameters
        sb.append(proposalSignature, 0, ArrayUtils.indexOf(proposalSignature, Signature.C_PARAM_START));

        sb.append(Signature.C_PARAM_START);
        char[][] parameterTypes = Signature.getParameterTypes(proposalSignature);
        for (char[] parameterType : parameterTypes) {
            sb.append(Signature.getTypeErasure(parameterType));
        }
        sb.append(Signature.C_PARAM_END);

        char[] returnType = Signature.getReturnType(proposalSignature);
        sb.append(Signature.getTypeErasure(returnType));

        char[][] exceptionTypes = Signature.getThrownExceptionTypes(proposalSignature);
        if (exceptionTypes.length > 0) {
            sb.append(Signature.C_EXCEPTION_START);
            for (char[] exceptionType : exceptionTypes) {
                sb.append(exceptionType);
            }
        }

        return sb.toString().toCharArray();
    }

    private static Optional<ReferenceBinding> getDeclaringType(CompletionProposal proposal, LookupEnvironment env) {
        char[] declarationSignature = proposal.getDeclarationSignature();
        if (declarationSignature[0] != 'L') {
            // Should not happen. The declaring type is always a reference type.
            return absent();
        }

        int semicolonIndex = ArrayUtils.indexOf(declarationSignature, ';');
        int greaterThanIndex = ArrayUtils.indexOf(declarationSignature, '<');
        if (greaterThanIndex == ArrayUtils.INDEX_NOT_FOUND) {
            greaterThanIndex = Integer.MAX_VALUE;
        }
        char[][] compoundName = splitOn('.', declarationSignature, 1, min(semicolonIndex, greaterThanIndex));

        return lookup(env, compoundName);
    }

    /**
     * @see <a href="https://www.eclipse.org/forums/index.php/m/1410672/">Forum discussion as to why the
     *      <code>ProblemReferenceBinding</code> handling is necessary</a>
     */
    private static Optional<ReferenceBinding> lookup(LookupEnvironment env, char[][] compoundName) {
        ReferenceBinding result = env.getType(compoundName);
        if (result instanceof ProblemReferenceBinding) {
            result = cast(((ProblemReferenceBinding) result).closestMatch());
        }
        return Optional.fromNullable(result);
    }
}
