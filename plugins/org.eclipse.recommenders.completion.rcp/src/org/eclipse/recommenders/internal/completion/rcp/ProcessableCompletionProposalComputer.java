/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - API and first implementation
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.proposals.ProcessableProposalFactory;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Creates more flexible completion proposals from original proposals
 */
public abstract class ProcessableCompletionProposalComputer extends JavaAllCompletionProposalComputer implements
        ICompletionListener, ICompletionListenerExtension2 {

    public static final CompletionProposal NULL_PROPOSAL = new CompletionProposal();

    public IProcessableProposalFactory proposalFactory;
    public IRecommendersCompletionContextFactory contextFactory;
    public Set<SessionProcessor> processors;
    public JavaContentAssistInvocationContext jdtContext;
    public IRecommendersCompletionContext crContext;
    public ContentAssistantFacade contentAssist;

    public ProcessableCompletionProposalComputer(IProcessableProposalFactory proposalFactory,
            IRecommendersCompletionContextFactory contextFactory) {
        this(proposalFactory, contextFactory, Sets.<SessionProcessor> newLinkedHashSet());
    }

    public ProcessableCompletionProposalComputer(IProcessableProposalFactory proposalFactory,
            IRecommendersCompletionContextFactory contextFactory, Set<SessionProcessor> computers) {
        this.proposalFactory = proposalFactory;
        this.contextFactory = contextFactory;
        this.processors = computers;
    }

    @Override
    public void sessionStarted() {
        // code looks odd? This method unregisters this instance from the last (!) source viewer
        // see unregisterCompletionListener for details
        unregisterCompletionListener();
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {

        List<ICompletionProposal> res = Lists.newLinkedList();

        if (!(context instanceof JavaContentAssistInvocationContext)) return res;

        storeContext(context);
        registerCompletionListener();

        fireStartSession(crContext);
        for (Entry<IJavaCompletionProposal, CompletionProposal> pair : crContext.getProposals().entrySet()) {
            IJavaCompletionProposal proposal =
                    ProcessableProposalFactory.create(pair.getValue(), pair.getKey(), jdtContext, proposalFactory);
            res.add(proposal);
            if (proposal instanceof IProcessableProposal) {
                fireProcessProposal((IProcessableProposal) proposal);
            }
        }
        fireEndComputation(res);
        fireAboutToShow(res);
        return res;
    }

    private void storeContext(ContentAssistInvocationContext context) {
        jdtContext = cast(context);
        crContext = contextFactory.create(jdtContext);
    }

    private void registerCompletionListener() {
        ITextViewer v = jdtContext.getViewer();
        if (!(v instanceof SourceViewer)) {
            return;
        }
        SourceViewer sv = (SourceViewer) v;
        contentAssist = sv.getContentAssistantFacade();
        contentAssist.addCompletionListener(this);
    }

    protected void fireStartSession(IRecommendersCompletionContext crContext) {
        for (SessionProcessor p : processors) {
            p.startSession(crContext);
        }
    }

    protected void fireProcessProposal(IProcessableProposal proposal) {
        for (SessionProcessor p : processors) {
            try {
                p.process(proposal);
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
        proposal.getProposalProcessorManager().prefixChanged(crContext.getPrefix());
    }

    protected void fireEndComputation(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : processors) {
            try {
                p.endSession(proposals);
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    protected void fireAboutToShow(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : processors) {
            try {
                p.aboutToShow(proposals);
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    @Override
    public void assistSessionStarted(ContentAssistEvent event) {
        // ignore
    }

    @Override
    public void assistSessionEnded(ContentAssistEvent event) {
        // ignore

        // calling unregister here seems like a good choice here but unfortunately isn't. "proposal applied" events are
        // fired after the sessionEnded event, and thus, we cannot use this method to unsubscribe from the current
        // editor. See unregisterCompletionListern for details.
    }

    @Override
    public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
        for (SessionProcessor p : processors) {
            try {
                p.selected(proposal);
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    @Override
    public void applied(ICompletionProposal proposal) {
        for (SessionProcessor p : processors) {
            try {
                p.applied(proposal);
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
        unregisterCompletionListener();
    }

    /*
     * Unregisters this computer from the last known content assist facade. This method is called in some unexpected
     * places (i.e., not in sessionEnded and similar methods) because unregistering in these methods would be too early
     * to get notified about apply events.
     */
    private void unregisterCompletionListener() {
        if (contentAssist != null) contentAssist.removeCompletionListener(this);
    }
}
