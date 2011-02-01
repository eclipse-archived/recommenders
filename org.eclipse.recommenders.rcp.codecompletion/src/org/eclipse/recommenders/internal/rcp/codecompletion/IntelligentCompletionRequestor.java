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
package org.eclipse.recommenders.internal.rcp.codecompletion;

import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class IntelligentCompletionRequestor extends CompletionRequestor {

    private int maximalProposalRelevance;

    private InternalCompletionContext ctx;

    private final Set<CompletionProposal> proposals = Sets.newHashSet();

    private final CompilationUnit cu;

    private IntelligentCompletionProposalCollector convertingCollector;

    public IntelligentCompletionRequestor(final CompilationUnit cu) {
        this.cu = cu;
        setRequireExtendedContext(true);
        setIgnored(CompletionProposal.KEYWORD, true);
        setIgnored(CompletionProposal.PACKAGE_REF, true);
        setIgnored(CompletionProposal.FIELD_IMPORT, true);
        setIgnored(CompletionProposal.LABEL_REF, true);
        setIgnored(CompletionProposal.METHOD_IMPORT, true);
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        proposals.add(proposal);
        maximalProposalRelevance = Math.max(getMaximalProposalRelevance(), proposal.getRelevance());
    }

    @Override
    public void acceptContext(final org.eclipse.jdt.core.CompletionContext context) {
        ctx = (InternalCompletionContext) context;
        convertingCollector = new IntelligentCompletionProposalCollector(cu);
        convertingCollector.acceptContext(context);
    }

    public Set<CompletionProposal> getProposals() {
        return proposals;
    }

    public IJavaCompletionProposal toJavaCompletionProposal(final CompletionProposal proposal) {
        return convertingCollector.createJavaCompletionProposal(proposal);
    }

    public InternalCompletionContext getCompletionContext() {
        return ctx;
    }

    public int getMaximalProposalRelevance() {
        return maximalProposalRelevance;
    }
}
