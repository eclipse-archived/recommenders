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
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.IntelligentCompletionContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

/**
 * Extracts the {@link CompletionTargetVariable} from an
 * {@link IIntelligentCompletionContext}.
 */
public final class CompletionTargetVariableBuilder {

    private IIntelligentCompletionContext context;
    private ITypeName receiverType;
    private String receiverName;
    private boolean needsConstructor;
    private Set<IMethodName> receiverCalls;

    /**
     * Hide the builder instance as it should directly be turned to the garbage
     * collector after the variable is built.
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
        return new CompletionTargetVariableBuilder().buildInvokedVariable(context);
    }

    /**
     * @param completionContext
     *            The context holding information about the completion request.
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request is invoked or which shall be constructed in
     *         case it is invoked while defining a new variable, e.g.
     *         <code>Button b<^Space></code>.
     */
    private CompletionTargetVariable buildInvokedVariable(final IIntelligentCompletionContext completionContext) {
        CompletionTargetVariable completionTargetVariable = null;
        context = completionContext;
        receiverType = context.getReceiverType();
        receiverName = context.getReceiverName();
        if (receiverType == null) {
            handleUnresolvedType();
        } else {
            needsConstructor = receiverType.equals(context.getExpectedType());
        }
        if (receiverType != null) {
            completionTargetVariable = buildInvokedVariable();
        }
        return completionTargetVariable;
    }

    /**
     * Try to find all required information when the target variable is not
     * fully declared in the context, i.e. its type is missing.
     */
    private void handleUnresolvedType() {
        if (receiverName == null || receiverName.isEmpty()) {
            receiverName = "this";
            receiverType = context.getEnclosingType();
        } else {
            final Variable resolvedVariable = ((IntelligentCompletionContext) context)
                    .findMatchingVariable(receiverName);
            if (resolvedVariable == null) {
                receiverType = VmTypeName.get("L" + receiverName);
                receiverName = "";
                needsConstructor = true;
            } else {
                receiverType = resolvedVariable.type;
                receiverCalls = resolvedVariable.getReceiverCalls();
            }
        }
    }

    /**
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request was invoked or which shall be constructed in
     *         case it was invoked while defining a new variable.
     */
    private CompletionTargetVariable buildInvokedVariable() {
        final int variableNameLength = getVariableNameLength();
        int documentOffset = context.getReplacementRegion().getOffset() - variableNameLength;
        if (needsConstructor) {
            documentOffset += context.getReplacementRegion().getLength();
        }
        // final int replacementLength =
        // context.getReplacementRegion().getLength() + variableNameLength;
        final int replacementLength = variableNameLength;
        return new CompletionTargetVariable(receiverName, receiverType, receiverCalls, new Region(documentOffset,
                replacementLength), needsConstructor);
    }

    /**
     * @return The length of the variable as used in the given context. This
     *         information is required to calculate the region to be replaced by
     *         the template code.
     */
    @SuppressWarnings("restriction")
    private int getVariableNameLength() {
        int variableNameLength = 0;
        if (needsConstructor) {
            final String completionNode = context.getCompletionNode().toString();
            variableNameLength = completionNode.substring(completionNode.indexOf(':')).length() - 2;
        } else if (receiverName != null && receiverName.length() > 0) {
            // For variables other than implicit "this", add space for ".".
            variableNameLength = receiverName.length() + 1;
        }
        return variableNameLength;
    }
}
