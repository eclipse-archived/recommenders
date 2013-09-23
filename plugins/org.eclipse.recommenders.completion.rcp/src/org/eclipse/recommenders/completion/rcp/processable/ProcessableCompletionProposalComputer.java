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
package org.eclipse.recommenders.completion.rcp.processable;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Iterator;
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
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.RcpPlugin;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Creates more flexible completion proposals from original proposals
 */
@SuppressWarnings("restriction")
public abstract class ProcessableCompletionProposalComputer extends JavaAllCompletionProposalComputer implements
        ICompletionListener, ICompletionListenerExtension2 {

    public static final CompletionProposal NULL_PROPOSAL = new CompletionProposal();

    public IProcessableProposalFactory proposalFactory;
    private IAstProvider astProvider;
    public Set<SessionProcessor> processors;
    private Set<SessionProcessor> active;
    public JavaContentAssistInvocationContext jdtContext;
    public IRecommendersCompletionContext crContext;
    public ContentAssistantFacade contentAssist;

    public ProcessableCompletionProposalComputer(IProcessableProposalFactory proposalFactory, IAstProvider astProvider) {
        this(proposalFactory, Sets.<SessionProcessor>newLinkedHashSet(), astProvider);
    }

    public ProcessableCompletionProposalComputer(IProcessableProposalFactory proposalFactory,
            Set<SessionProcessor> processors, IAstProvider astProvider) {
        this.proposalFactory = proposalFactory;
        this.processors = processors;
        this.astProvider = astProvider;
    }

    @Override
    public void sessionStarted() {
        active = Sets.newHashSet(processors);
        // code looks odd? This method unregisters this instance from the last (!) source viewer
        // see unregisterCompletionListener for details
        unregisterCompletionListener();
    }

    @Override
    public void sessionEnded() {
        fireAboutToClose();
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {

        List<ICompletionProposal> res = Lists.newLinkedList();

        if (!(context instanceof JavaContentAssistInvocationContext)) {
            return res;
        }

        storeContext(context);
        registerCompletionListener();

        fireStartSession(crContext);
        for (Entry<IJavaCompletionProposal, CompletionProposal> pair : crContext.getProposals().entrySet()) {
            IJavaCompletionProposal proposal = ProcessableProposalFactory.create(pair.getValue(), pair.getKey(),
                    jdtContext, proposalFactory);
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
        crContext = new RecommendersCompletionContext(jdtContext, astProvider);
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
        for (Iterator<SessionProcessor> it = active.iterator(); it.hasNext();) {
            SessionProcessor p = it.next();
            try {
                boolean interested = p.startSession(crContext);
                if (!interested) {
                    it.remove();
                }
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    protected void fireProcessProposal(IProcessableProposal proposal) {
        for (SessionProcessor p : active) {
            try {
                proposal.getRelevance();
                p.process(proposal);
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
        proposal.getProposalProcessorManager().prefixChanged(crContext.getPrefix());
    }

    protected void fireEndComputation(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : active) {
            try {
                p.endSession(proposals);
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    protected void fireAboutToShow(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : active) {
            try {
                p.aboutToShow(proposals);
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    protected void fireAboutToClose() {
        for (SessionProcessor p : active) {
            try {
                p.aboutToClose();
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
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
        for (SessionProcessor p : active) {
            try {
                p.selected(proposal);
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
            }
        }
    }

    @Override
    public void applied(ICompletionProposal proposal) {
        for (SessionProcessor p : active) {
            try {
                p.applied(proposal);
            } catch (Exception e) {
                RcpPlugin.logError(e, "session processor '%s' failed with exception.", p.getClass());
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
        if (contentAssist != null) {
            contentAssist.removeCompletionListener(this);
        }
    }
}
