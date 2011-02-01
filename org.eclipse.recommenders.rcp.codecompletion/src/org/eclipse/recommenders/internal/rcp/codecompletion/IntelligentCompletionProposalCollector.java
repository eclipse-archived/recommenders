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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Used to convert a {@link CompletionProposal} to an
 * {@link IJavaCompletionProposal}. Therefore it raises the visibility of
 * {@link #createJavaCompletionProposal(CompletionProposal)} from protected to
 * public.
 * 
 */
public final class IntelligentCompletionProposalCollector extends CompletionProposalCollector {

    public IntelligentCompletionProposalCollector(final ICompilationUnit cu) {
        super(cu);
    }

    /**
     * Used to create {@link IJavaCompletionProposal}s from standard
     * {@link CompletionProposal} s. Overridden to increase visibility to
     * public.
     */
    @Override
    public IJavaCompletionProposal createJavaCompletionProposal(final CompletionProposal proposal) {
        return super.createJavaCompletionProposal(proposal);
    }
}
