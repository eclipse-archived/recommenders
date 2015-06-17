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

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Optional.absent;
import static org.eclipse.jdt.core.compiler.CharOperation.NO_CHAR;
import static org.eclipse.recommenders.utils.LogMessages.LOG_WARNING_REFLECTION_FAILED;
import static org.eclipse.recommenders.utils.Logs.log;
import static org.eclipse.recommenders.utils.Reflections.getDeclaredField;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

@SuppressWarnings("restriction")
public final class ProposalUtils {

    private ProposalUtils() {
    }

    private static final IMethodName OBJECT_CLONE = VmMethodName.get("Ljava/lang/Object.clone()Ljava/lang/Object;"); //$NON-NLS-1$

    private static final char[] INIT = "<init>".toCharArray(); //$NON-NLS-1$
    private static final char[] JAVA_LANG_OBJECT = "Ljava.lang.Object;".toCharArray(); //$NON-NLS-1$

    /**
     * Workaround needed to handle proposals with generic signatures properly.
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=380203">Bug 380203</a>.
     */
    private static final Field ORIGINAL_SIGNATURE = getDeclaredField(InternalCompletionProposal.class,
            "originalSignature") //$NON-NLS-1$
                    .orNull();

    public static Optional<IMethodName> toMethodName(CompletionProposal proposal) {
        Preconditions.checkArgument(isKindSupported(proposal));

        if (isArrayCloneMethod(proposal)) {
            return Optional.of(OBJECT_CLONE);
        }

        StringBuilder builder = new StringBuilder();

        // Declaring type
        char[] declarationSignature = proposal.getDeclarationSignature();
        char[] binaryTypeSignature = getBinaryTypeSignatureCopy(declarationSignature);
        char[] erasedBinaryTypeSignature = Signature.getTypeErasure(binaryTypeSignature);
        CharOperation.replace(erasedBinaryTypeSignature, '.', '/');
        builder.append(erasedBinaryTypeSignature, 0, erasedBinaryTypeSignature.length - 1);

        builder.append('.');

        // Method nane
        builder.append(proposal.isConstructor() ? INIT : proposal.getName());

        builder.append('(');

        // Parameter types
        char[] signature = getSignature(proposal);
        char[][] typeParameters = Signature.getTypeParameters(declarationSignature);
        char[][] parameterTypes = Signature.getParameterTypes(signature);
        for (char[] parameterType : parameterTypes) {
            appendType(builder, parameterType, typeParameters);
        }

        builder.append(')');

        // Return type
        appendType(builder, Signature.getReturnType(signature), typeParameters);

        String methodName = builder.toString();
        try {
            return Optional.<IMethodName>of(VmMethodName.get(methodName));
        } catch (Exception e) {
            log(LogMessages.ERROR_SYNTATICALLY_INCORRECT_METHOD_NAME, e, methodName, toLogString(proposal));
            return absent();
        }
    }

    /**
     * Ensures that the separator of inner and outer types is always a dollar sign.
     * 
     * <p>
     * This is necessary, as JDT uses a dot rather than dollar sign to separate inner and outer type <em>if</em> the
     * outer type is parameterized.
     * </p>
     * 
     * <p>
     * Examples:
     * </p>
     * 
     * <ul>
     * <li><code>org.example.Outer$Inner&lt:java.lang.String&gt;</code> ->
     * <code>org.example.Outer$Inner&lt:java.lang.String&gt;</code></li>
     * <li><code>org.example.Outer&lt:java.lang.String&gt;.Inner</code> ->
     * <code>org.example.Outer&lt:java.lang.String&gt;$Inner</code></li>
     * <ul>
     */
    private static char[] getBinaryTypeSignatureCopy(char[] parameterizedTypeSignature) {
        char[] binaryTypeSignature = Arrays.copyOf(parameterizedTypeSignature, parameterizedTypeSignature.length);
        int nextDot = -1;
        while ((nextDot = CharOperation.indexOf(Signature.C_DOT, binaryTypeSignature, nextDot + 1)) > 0) {
            if (binaryTypeSignature[nextDot - 1] == Signature.C_GENERIC_END) {
                binaryTypeSignature[nextDot] = Signature.C_DOLLAR;
            }
        }
        return binaryTypeSignature;
    }

    private static void appendType(StringBuilder builder, char[] type, char[][] typeParameters) {
        switch (Signature.getTypeSignatureKind(type)) {
        case Signature.TYPE_VARIABLE_SIGNATURE:
            char[] typeVariableName = CharOperation.subarray(type, 1, type.length - 1);
            char[] resolvedTypeVariable = resolveTypeVariable(typeVariableName, typeParameters);
            builder.append(CharOperation.replaceOnCopy(resolvedTypeVariable, '.', '/'));
            break;
        case Signature.ARRAY_TYPE_SIGNATURE:
            int dimensions = Signature.getArrayCount(type);
            builder.append(type, 0, dimensions);
            appendType(builder, Signature.getElementType(type), typeParameters);
            break;
        default:
            char[] erasedParameterType = Signature.getTypeErasure(type);
            builder.append(CharOperation.replaceOnCopy(erasedParameterType, '.', '/'));
            break;
        }
    }

    private static char[] resolveTypeVariable(char[] typeVariableName, char[][] typeParameters) {
        for (char[] typeParameter : typeParameters) {
            if (CharOperation.equals(typeVariableName, Signature.getTypeVariable(typeParameter))) {
                char[][] typeParameterBounds = Signature.getTypeParameterBounds(typeParameter);
                if (typeParameterBounds.length > 0) {
                    return typeParameterBounds[0];
                } else {
                    return JAVA_LANG_OBJECT;
                }
            }
        }
        // No match found. Assume Object.
        return JAVA_LANG_OBJECT;
    }

    private static String toLogString(CompletionProposal proposal) {
        if (proposal == null) {
            return "null proposal"; //$NON-NLS-1$
        }
        return new StringBuilder().append(firstNonNull(proposal.getDeclarationSignature(), NO_CHAR)).append('#')
                .append(firstNonNull(proposal.getName(), NO_CHAR)).append('#')
                .append(firstNonNull(proposal.getSignature(), NO_CHAR)).toString();
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

    private static boolean isArrayCloneMethod(CompletionProposal proposal) {
        if (proposal.isConstructor()) {
            // Not a method proposal
            return false;
        }

        char[] declarationSignature = proposal.getDeclarationSignature();
        if (declarationSignature[0] != '[') {
            // Not an array
            return false;
        }

        if (!CharOperation.equals(TypeConstants.CLONE, proposal.getName())) {
            // Not named clone
            return false;
        }

        char[] signature = proposal.getSignature();
        if (signature.length != declarationSignature.length + 2 || signature[0] != '(' || signature[1] != ')') {
            // Overload of real (no-args) clone method
            return false;
        }

        if (!CharOperation.endsWith(signature, declarationSignature)) {
            // Wrong return type
            return false;
        }

        return true;
    }

    private static char[] getSignature(CompletionProposal proposal) {
        char[] signature = null;
        if (canUseReflection(proposal)) {
            try {
                signature = (char[]) ORIGINAL_SIGNATURE.get(proposal);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log(LOG_WARNING_REFLECTION_FAILED, e, ORIGINAL_SIGNATURE);
            }
        }
        return signature != null ? signature : proposal.getSignature();
    }

    private static boolean canUseReflection(CompletionProposal proposal) {
        return proposal instanceof InternalCompletionProposal && ORIGINAL_SIGNATURE != null
                && ORIGINAL_SIGNATURE.isAccessible();
    }
}
