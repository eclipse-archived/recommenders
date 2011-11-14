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
package org.eclipse.recommenders.internal.completion.rcp.templates;

import java.util.Set;

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.Sets;

/**
 * Extracts the {@link CompletionTargetVariable} from a given
 * {@link IIntelligentCompletionContext}.
 */
public final class CompletionTargetVariableBuilder {

    private ITypeName receiverType;
    private String receiverName;
    private int replacementOffset;
    private boolean needsConstructor;
    private final Set<IMethodName> receiverCalls = Sets.newHashSet();

    private boolean isCallOnThis;

    /**
     * Hide the builder instantiation as it should directly be turned to the
     * garbage collector after the variable is built.
     */
    private CompletionTargetVariableBuilder() {
    }

    /**
     * @param context
     *            The context from which the completion request was invoked.
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request is invoked or which shall be constructed in
     *         case it is invoked while defining a new variable, e.g.
     *         <code>Button b<^Space></code>.
     */
    public static CompletionTargetVariable createInvokedVariable(final IIntelligentCompletionContext context) {
        return new CompletionTargetVariableBuilder().buildInvokedVariable(context);
    }

    /**
     * @param context
     *            The context from which the completion request was invoked.
     * @return The {@link CompletionTargetVariable} representing the variable on
     *         which the request is invoked or which shall be constructed in
     *         case it is invoked while defining a new variable, e.g. using
     *         <code>Button b<^Space></code>.
     */
    private CompletionTargetVariable buildInvokedVariable(final IIntelligentCompletionContext context) {
        receiverType = context.getReceiverType();
        receiverName = context.getReceiverName() == null ? "" : context.getReceiverName();
        replacementOffset = context.getReplacementRegion().getOffset();
        if (receiverType == null) {
            handleUnresolvedType(context);
        } else if (context.getVariable() != null && context.getVariable().isThis()) {
            receiverType = Checks.ensureIsNotNull(context.getVariable().getType());
            isCallOnThis = true;
            receiverName = context.getPrefixToken();
        } else {
            // REVIEW: Example? No idea why this is needed?
            // Button b<^Space>
            // Button x = b. --> new test case? StringBuilder.append("")
            needsConstructor = receiverType.equals(context.getExpectedType());
        }
        return receiverType == null ? null : buildCompletionTargetVariable(context);
    }

    /**
     * Tries to find all required information when the target variable is not
     * fully declared in the context, i.e. its type is missing, and stores them
     * in the attributes of the builder.
     */
    private void handleUnresolvedType(final IIntelligentCompletionContext context) {
        if (receiverName.isEmpty()) {
            receiverType = context.getVariable() == null ? null : context.getVariable().getType();
            isCallOnThis = true;
        } else {
            resolveTypeFromReceiverName(context);
        }
    }

    /**
     * Either finds a variable declaration matching the receiver name or assumes
     * the given name indicates a new type, e.g. in <code>Button<^Space></code>.
     */
    private void resolveTypeFromReceiverName(final IIntelligentCompletionContext context) {
        final Variable resolvedVariable = context.findMatchingVariable(receiverName);
        if (resolvedVariable == null) {
            receiverType = VmTypeName.get(String.format("L%s", receiverName));
            receiverName = "";
            needsConstructor = true;
            replacementOffset += context.getReplacementRegion().getLength();
        } else {
            // TODO: getReceiverCalls currently doesn't work and will therefore
            // cause the recommender to propose patterns with constructors, even
            // though they might already exist.
            // receiverType = resolvedVariable.type;
            // receiverCalls = resolvedVariable.getReceiverCalls();
        }
    }

    /**
     * @return A {@link CompletionTargetVariable} object representing the
     *         variable on which the request was invoked or which shall be
     *         constructed in case it was invoked while defining a new variable.
     */
    private CompletionTargetVariable buildCompletionTargetVariable(final IIntelligentCompletionContext context) {
        final int variableNameLength = getVariableNameLength(context);
        final Region region = new Region(replacementOffset - variableNameLength, variableNameLength);
        return new CompletionTargetVariable(receiverName, receiverType, receiverCalls, region, isCallOnThis,
                needsConstructor, context);
    }

    /**
     * @return The length of the variable as used in the given context. This
     *         information is required to calculate the region to be replaced by
     *         the template code.
     */
    @SuppressWarnings("restriction")
    private int getVariableNameLength(final IIntelligentCompletionContext context) {
        int variableNameLength = 0;
        if (needsConstructor) {
            // TODO: Gets the token to be replaced from the completion node,
            // e.g. "<CompleteOnLocalName:Button b>;". To be refactored.
            // REVIEW: is node.name appropriate?
            // REVIEW double check - ICtx doesn't deliver the type of this node?
            final String node = context.getCompletionNode().toString();
            variableNameLength = node.substring(node.indexOf(':') + 1, node.indexOf('>')).length();
        } else if (receiverName.length() > 0 && !isCallOnThis) {
            // For variables other than implicit "this", add space for ".".
            variableNameLength = receiverName.length() + 1;
        }
        return variableNameLength;
    }
}
