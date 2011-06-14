/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.resolvers;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;

@SuppressWarnings("restriction")
public class AnonymousMemberAccessVariableUsageResolver implements IVariableUsageResolver {

    private IIntelligentCompletionContext ctx;

    @Override
    public boolean canResolve(final IIntelligentCompletionContext ctx) {
        this.ctx = ensureIsNotNull(ctx);
        final ASTNode completionNode = ctx.getCompletionNode();
        final boolean isThis = "this".equals(ctx.getReceiverName());
        return !isThis && completionNode instanceof CompletionOnMemberAccess;
    }

    @Override
    public Set<IMethodName> getReceiverMethodInvocations() {
        return Collections.emptySet();
    }

    @Override
    public Variable getResolvedVariable() {
        final Variable var = ctx.getVariable();
        final Variable res = Variable.create(null, var.getType(), ctx.getEnclosingMethod());
        res.kind = Kind.RETURN;
        return res;

    }
}
