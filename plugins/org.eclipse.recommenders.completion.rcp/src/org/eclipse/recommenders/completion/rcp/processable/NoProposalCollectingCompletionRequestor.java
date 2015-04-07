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
package org.eclipse.recommenders.completion.rcp.processable;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Map;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class NoProposalCollectingCompletionRequestor extends CompletionRequestor {

    private Logger log = LoggerFactory.getLogger(getClass());
    private InternalCompletionContext compilerContext;

    public NoProposalCollectingCompletionRequestor() {
        super(false);
    }

    @Override
    public boolean isIgnored(final int completionProposalKind) {
        return true;
    }

    @Override
    public boolean isAllowingRequiredProposals(final int proposalKind, final int requiredProposalKind) {
        return false;
    }

    @Override
    public boolean isExtendedContextRequired() {
        return true;
    }

    @Override
    public String[] getFavoriteReferences() {
        return CharOperation.NO_STRINGS;
    }

    @Override
    public void acceptContext(final CompletionContext context) {
        compilerContext = cast(context);
    }

    @Override
    public void accept(final CompletionProposal compilerProposal) {
    }

    @Override
    public void completionFailure(IProblem problem) {
        log.debug(problem.toString());
    }

    public InternalCompletionContext getCoreContext() {
        return compilerContext;
    }

    public Map<IJavaCompletionProposal, CompletionProposal> getProposals() {
        return Maps.newHashMap();
    }
}
