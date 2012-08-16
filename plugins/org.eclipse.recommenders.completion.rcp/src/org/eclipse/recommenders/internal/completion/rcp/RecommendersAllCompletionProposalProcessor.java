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
package org.eclipse.recommenders.internal.completion.rcp;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.completion.rcp.proposals.ProcessableProposalFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

public class RecommendersAllCompletionProposalProcessor extends ProcessableCompletionProposalComputer {

    private SessionProcessorDescriptor[] descriptors;
    static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory";
    static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory";

    public static String CATEGORY_ID = "org.eclipse.recommenders.completion.rcp.category.completion.all";

    @Inject
    public RecommendersAllCompletionProposalProcessor(SessionProcessorDescriptor[] descriptors,
            ProcessableProposalFactory proposalFactory, IRecommendersCompletionContextFactory contextFactory) {
        super(new ProcessableProposalFactory(), contextFactory);
        this.descriptors = descriptors;
    }

    @Override
    public void sessionStarted() {
        super.sessionStarted();
        processors.clear();
        for (SessionProcessorDescriptor d : descriptors) {
            if (d.isEnabled()) {
                processors.add(d.getProcessor());
            }
        }
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {

        if (!shouldReturnResults()) return Collections.emptyList();

        return super.computeCompletionProposals(context, monitor);
    }

    @VisibleForTesting
    protected boolean shouldReturnResults() {
        Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (cats.contains(CATEGORY_ID)) {
            // we are excluded on default tab?
            // then we are not on default tab NOW. We are on a subsequent tab.
            // then make completions:
            return true;
        }

        if (isJdtAllEnabled(cats) || isMylynInstalledAndEnabled(cats)) {
            // do not compute any recommendations and deactivate yourself in background
            new DisableContentAssistCategoryJob(CATEGORY_ID).schedule(300);
            return false;
        }
        return true;
    }

    private boolean isMylynInstalledAndEnabled(Set<String> cats) {
        return isMylynInstalled() && !cats.contains(MYLYN_ALL_CATEGORY);
    }

    private boolean isJdtAllEnabled(Set<String> cats) {
        return !cats.contains(JDT_ALL_CATEGORY);
    }

    public static boolean isMylynInstalled() {
        CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
        for (CompletionProposalCategory cat : reg.getProposalCategories()) {
            if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                return true;
            }
        }
        return false;
    }

}
