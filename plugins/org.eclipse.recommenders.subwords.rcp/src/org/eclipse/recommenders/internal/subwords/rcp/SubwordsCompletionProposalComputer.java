/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableProposalFactory;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

public class SubwordsCompletionProposalComputer extends ProcessableCompletionProposalComputer {
    public static final String CATEGORY_ID = "org.eclipse.recommenders.subwords.rcp.category";

    @Inject
    public SubwordsCompletionProposalComputer(IAstProvider astProvider) {
        this(new SubwordsSessionProcessor(astProvider), astProvider);
    }

    @VisibleForTesting
    public SubwordsCompletionProposalComputer(SubwordsSessionProcessor processor, IAstProvider astProvider) {
        super(new ProcessableProposalFactory(), Sets.<SessionProcessor>newHashSet(processor), astProvider);
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        if (!shouldMakeProposals()) {
            return Collections.emptyList();
        }
        return super.computeCompletionProposals(context, monitor);
    }

    @VisibleForTesting
    protected boolean shouldMakeProposals() {
        final Set<String> excluded = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (excluded.contains(CATEGORY_ID)) {
            // we are excluded on default tab? Then we are not on default tab NOW. We are on a subsequent tab and should
            // make completions:
            return true;
        }
        // disable and stop computing.
        new DisableContentAssistCategoryJob(CATEGORY_ID).schedule(300);
        return false;
    }
}
