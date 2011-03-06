/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import java.util.Set;

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.IntelligentCompletionContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

/**
 * Extracts the {@link CompletionTargetVariable} from an
 * {@link IIntelligentCompletionContext}.
 */
public final class CompletionTargetVariableBuilder {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CompletionTargetVariableBuilder() {
    }

    /**
     * @param context
     *            The context holding information about the completion request.
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request is invoked or which shall be constructed in
     *         case it is invoked while defining a new variable, e.g.
     *         <code>Button b<^Space></code>.
     */
    public static CompletionTargetVariable createInvokedVariable(final IIntelligentCompletionContext context) {
        ITypeName receiverType = context.getReceiverType();
        String receiverName = context.getReceiverName();
        Set<IMethodName> receiverCalls = null;

        final boolean needsConstructor = receiverType != null && receiverType.equals(context.getExpectedType());

        if (receiverType == null) {
            if (receiverName == null || receiverName.isEmpty()) {
                receiverName = "this";
                receiverType = context.getEnclosingType();
            } else {
                final Variable resolvedVariable = ((IntelligentCompletionContext) context)
                        .findMatchingVariable(receiverName);
                if (resolvedVariable != null) {
                    receiverType = resolvedVariable.type;
                    receiverCalls = resolvedVariable.getReceiverCalls();
                }
            }
        }

        return createInvokedVariable(receiverName, receiverType, receiverCalls, context, needsConstructor);
    }

    /**
     * @param receiverName
     *            The variable name if it could be extracted from the context.
     * @param receiverType
     *            The type of the variable on which completion is requested.
     * @param receiverCalls
     * @param context
     *            The context holding information about the completion request.
     * @param needsConstructor
     *            True, if the completion is invoked while a new variable is
     *            defined, e.g. <code>Button b<^Space></code>.
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request was invoked or which shall be constructed in
     *         case it was invoked while defining a new variable.
     */
    private static CompletionTargetVariable createInvokedVariable(final String receiverName,
            final ITypeName receiverType, final Set<IMethodName> receiverCalls,
            final IIntelligentCompletionContext context, final boolean needsConstructor) {
        CompletionTargetVariable completionTargetVariable = null;
        if (receiverType != null) {
            int variableNameLength = 0;
            if (!needsConstructor && receiverName != null && receiverName.length() > 0) {
                // For variables other than implicit "this", add space for ".".
                variableNameLength = receiverName.length() + 1;
            }
            final int documentOffset = context.getReplacementRegion().getOffset() - variableNameLength;
            final int replacementLength = context.getReplacementRegion().getLength() + variableNameLength;
            completionTargetVariable = new CompletionTargetVariable(receiverName, receiverType, receiverCalls,
                    new Region(documentOffset, replacementLength), needsConstructor);
        }
        return completionTargetVariable;
    }
}
