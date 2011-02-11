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

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

/**
 * Constructs a <code>Receiver</code> from an
 * <code>IIntelligentCompletionContext</code>.
 */
final class CompletionTargetVariableBuilder {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CompletionTargetVariableBuilder() {
    }

    public static CompletionTargetVariable createInvokedVariable(final IIntelligentCompletionContext context) {
        ITypeName receiverType = context.getReceiverType();
        String receiverName = context.getReceiverName();

        int variableNameLength = 0;
        final boolean needsConstructor = needsConstructor(receiverName);
        if (needsConstructor) {
            receiverName = receiverName.substring(0, receiverName.length() - 1);
        } else if (receiverName.length() > 0) {
            // For existing variables other than implicit "this", add a dot.
            variableNameLength = receiverName.length() + 1;
        }

        if (receiverName.isEmpty() && receiverType == null) {
            receiverName = "this";
            receiverType = context.getEnclosingType();
        }

        return createInvokedVariable(receiverName, receiverType, context, variableNameLength, needsConstructor);
    }

    private static CompletionTargetVariable createInvokedVariable(final String receiverName,
            final ITypeName receiverType, final IIntelligentCompletionContext context, final int variableNameLength,
            final boolean needsConstructor) {
        CompletionTargetVariable completionTargetVariable = null;
        if (receiverType != null) {
            int documentOffset;
            if (needsConstructor) {
                documentOffset = context.getReplacementRegion().getOffset();
            } else {
                documentOffset = context.getInvocationOffset() - variableNameLength;
            }
            final int replacementLength = context.getReplacementRegion().getLength() + variableNameLength;
            completionTargetVariable = new CompletionTargetVariable(receiverName, receiverType, new Region(
                    documentOffset, replacementLength), needsConstructor);
        }
        return completionTargetVariable;
    }

    private static boolean needsConstructor(final String receiverName) {
        // TODO: Discuss with advisor :-) Bug in Advisors code?
        return receiverName.endsWith(" ");
    }
}
