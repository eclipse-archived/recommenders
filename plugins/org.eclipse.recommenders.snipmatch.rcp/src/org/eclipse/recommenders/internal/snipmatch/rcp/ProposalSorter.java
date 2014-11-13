/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.util.Comparator;

import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class ProposalSorter extends AbstractProposalSorter {

    private static final Comparator<Integer> LOWER_PRIORITY_FIRST = Ordering.natural();

    private static final ICompletionProposalSorter FALLBACK_SORTER = new RelevanceSorter();

    @Override
    public int compare(ICompletionProposal lhs, ICompletionProposal rhs) {
        if (lhs instanceof SnippetProposal && rhs instanceof SnippetProposal) {
            SnippetProposal lhsSnippet = (SnippetProposal) lhs;
            SnippetProposal rhsSnippet = (SnippetProposal) rhs;

            return ComparisonChain.start()
                    .compare(lhsSnippet.getRepositoryRelevance(), rhsSnippet.getRepositoryRelevance())
                    .compare(rhsSnippet.getRelevance(), lhsSnippet.getRelevance())
                    .compare(lhsSnippet.getSnippet().getName(), rhsSnippet.getSnippet().getName()).result();
        } else if (lhs instanceof RepositoryProposal && rhs instanceof RepositoryProposal) {
            RepositoryProposal lhsRepository = (RepositoryProposal) lhs;
            RepositoryProposal rhsRepository = (RepositoryProposal) rhs;

            return LOWER_PRIORITY_FIRST.compare(lhsRepository.getRepositoryPriority(),
                    rhsRepository.getRepositoryPriority());
        } else if (lhs instanceof RepositoryProposal && rhs instanceof SnippetProposal) {
            return compareRepositoryWithSnippet((RepositoryProposal) lhs, (SnippetProposal) rhs);
        } else if (lhs instanceof SnippetProposal && rhs instanceof RepositoryProposal) {
            return -compareRepositoryWithSnippet((RepositoryProposal) rhs, (SnippetProposal) lhs);
        } else {
            return FALLBACK_SORTER.compare(lhs, rhs);
        }
    }

    private int compareRepositoryWithSnippet(RepositoryProposal repository, SnippetProposal snippet) {
        int comparison = LOWER_PRIORITY_FIRST.compare(repository.getRepositoryPriority(),
                snippet.getRepositoryRelevance());
        return comparison != 0 ? comparison : -1;
    }
}
