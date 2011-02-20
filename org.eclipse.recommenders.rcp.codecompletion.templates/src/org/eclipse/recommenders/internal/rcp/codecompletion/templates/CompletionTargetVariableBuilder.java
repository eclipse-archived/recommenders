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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

/**
 * Extracts the {@link CompletionTargetVariable} from an
 * {@link IIntelligentCompletionContext}.
 */
final class CompletionTargetVariableBuilder {

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

        final boolean needsConstructor = needsConstructor(receiverName);
        if (needsConstructor) {
            receiverName = StringUtils.chop(receiverName);
        }

        if (receiverName.isEmpty() && receiverType == null) {
            receiverName = "this";
            receiverType = context.getEnclosingType();
        }

        return createInvokedVariable(receiverName, receiverType, context, needsConstructor);
    }

    /**
     * @param receiverName
     *            The variable name if it could be extracted from the context.
     * @param receiverType
     *            The type of the variable on which completion is requested.
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
            final ITypeName receiverType, final IIntelligentCompletionContext context, final boolean needsConstructor) {
        CompletionTargetVariable completionTargetVariable = null;
        if (receiverType != null) {
            int variableNameLength = 0;
            if (!needsConstructor && receiverName.length() > 0) {
                // For variables other than implicit "this", add space for ".".
                variableNameLength = receiverName.length() + 1;
            }
            final int documentOffset = context.getReplacementRegion().getOffset() - variableNameLength;
            final int replacementLength = context.getReplacementRegion().getLength() + variableNameLength;
            completionTargetVariable = new CompletionTargetVariable(receiverName, receiverType, new Region(
                    documentOffset, replacementLength), needsConstructor);
        }
        return completionTargetVariable;
    }

    /**
     * @param receiverName
     *            The target variable's name as given by the context.
     * @return True, if the completion is invoked while a new variable is
     *         defined, e.g. <code>Button b<^Space></code>.
     */
    @Provisional
    private static boolean needsConstructor(final String receiverName) {
        // TODO: Discuss with advisor :-) Bug in Advisors code?
        return receiverName.endsWith(" ");
    }
}
