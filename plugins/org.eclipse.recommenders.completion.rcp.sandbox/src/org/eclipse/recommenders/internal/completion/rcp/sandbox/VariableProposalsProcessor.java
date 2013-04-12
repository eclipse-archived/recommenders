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

import static org.eclipse.recommenders.internal.completion.rcp.ProcessableCompletionProposalComputer.NULL_PROPOSAL;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.SimpleProposalProcessor;

public final class VariableProposalsProcessor extends SessionProcessor {
    private static SimpleProposalProcessor EXACT = new SimpleProposalProcessor(1 << 26, null);

    static final Set<Class<?>> SUPPORTED_COMPLETION_NODES = new HashSet<Class<?>>() {
        {
            add(CompletionOnSingleNameReference.class);
        }
    };

    @Override
    public void startSession(IRecommendersCompletionContext context) {
        ASTNode completion = context.getCompletionNode().orNull();
        if (completion == null || !SUPPORTED_COMPLETION_NODES.contains(completion.getClass())) {
            return;
        }
    }

    @Override
    public void process(IProcessableProposal proposal) throws JavaModelException {

        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.FIELD_REF:
        case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.LOCAL_VARIABLE_REF:
            proposal.getProposalProcessorManager().addProcessor(EXACT);
            break;
        }
    }
}
