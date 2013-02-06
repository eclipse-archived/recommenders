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
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import static org.apache.commons.lang3.ArrayUtils.isEquals;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.eclipse.jdt.core.Signature.getReturnType;
import static org.eclipse.recommenders.internal.completion.rcp.ProcessableCompletionProposalComputer.NULL_PROPOSAL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.SimpleProposalProcessor;

public class Subwords2SessionProcessor extends SessionProcessor {

    SimpleProposalProcessor processor = new SimpleProposalProcessor(5, "contains"); //$NON-NLS-1$
    private boolean skip = false;
    private String varName;

    @Override
    public void startSession(IRecommendersCompletionContext ctx) {
        ASTNode node = ctx.getCompletionNodeParent().orNull();
        if (node instanceof Assignment) {
            varName = findVarName((Assignment) node);
        } else if (node instanceof LocalDeclaration) {
            varName = findVarName((LocalDeclaration) node);
        }
        skip = StringUtils.length(varName) < 3;
    }

    private String findVarName(final LocalDeclaration node) {
        return toLowerCaseString(node.name);
    }

    String toLowerCaseString(final char[] name) {
        if (name == null) {
            return null;
        }
        return String.valueOf(name).toLowerCase();
    }

    private String findVarName(final Assignment node) {
        if (!(node.lhs instanceof SingleNameReference)) {
            return null;
        }
        SingleNameReference lhs = (SingleNameReference) node.lhs;
        return toLowerCaseString(lhs.token);
    }

    @Override
    public void process(final IProcessableProposal proposal) {
        if (skip) return;

        CompletionProposal core = proposal.getCoreProposal().or(NULL_PROPOSAL);
        ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
        String name = null;

        switch (core.getKind()) {
        case CompletionProposal.FIELD_REF:
        case CompletionProposal.LOCAL_VARIABLE_REF:
            name = String.valueOf(core.getName());
            if (containsIgnoreCase(name, varName)) {
                mgr.addProcessor(processor);
            }

            break;
        case CompletionProposal.METHOD_REF:
            name = String.valueOf(core.getName());
            if (containsIgnoreCase(name, varName) && !isVoid(core)) {
                mgr.addProcessor(processor);
            }
        }
    }

    private boolean isVoid(CompletionProposal core) {
        return isEquals(getReturnType(core.getSignature()), new char[] { 'V' });
    }
}
