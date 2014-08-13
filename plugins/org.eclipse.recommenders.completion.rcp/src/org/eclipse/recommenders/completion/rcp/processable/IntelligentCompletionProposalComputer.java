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
package org.eclipse.recommenders.completion.rcp.processable;

import static org.eclipse.recommenders.completion.rcp.processable.ProcessableProposalFactory.create;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.*;
import static org.eclipse.recommenders.internal.completion.rcp.Constants.*;
import static org.eclipse.recommenders.internal.completion.rcp.LogMessages.LOG_ERROR_SESSION_PROCESSOR_FAILED;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.PreferenceConstants;
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
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.completion.rcp.EmptyCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.EnableCompletionProposal;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings({ "restriction", "rawtypes" })
public class IntelligentCompletionProposalComputer extends JavaAllCompletionProposalComputer implements
ICompletionListener, ICompletionListenerExtension2 {

    private static final Logger LOG = LoggerFactory.getLogger(IntelligentCompletionProposalComputer.class);

    private final CompletionRcpPreferences preferences;
    private final IAstProvider astProvider;
    private final SharedImages images;
    private final Map<CompletionContextKey, ICompletionContextFunction> functions;
    private final IProcessableProposalFactory proposalFactory = new ProcessableProposalFactory();

    private final Set<SessionProcessor> processors = Sets.newLinkedHashSet();
    private final Set<SessionProcessor> activeProcessors = Sets.newLinkedHashSet();

    // Set in storeContext
    public JavaContentAssistInvocationContext jdtContext;
    public IRecommendersCompletionContext crContext;

    public ContentAssistantFacade contentAssist;

    @Inject
    public IntelligentCompletionProposalComputer(CompletionRcpPreferences preferences, IAstProvider astProvider,
            SharedImages images, Map<CompletionContextKey, ICompletionContextFunction> functions) {
        this.preferences = preferences;
        this.astProvider = astProvider;
        this.images = images;
        this.functions = functions;
    }

    @Override
    public void sessionStarted() {
        processors.clear();
        for (SessionProcessorDescriptor d : preferences.getEnabledSessionProcessors()) {
            try {
                processors.add(d.getProcessor());
            } catch (CoreException e) {
                LOG.error("Failed to create session processor", e); //$NON-NLS-1$
            }
        }
        activeProcessors.clear();
        activeProcessors.addAll(processors);
        // code looks odd? This method unregisters this instance from the last(!) source viewer see
        // unregisterCompletionListener for details
        unregisterCompletionListener();
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        if (!(context instanceof JavaContentAssistInvocationContext)) {
            return Collections.emptyList();
        }

        storeContext(context);

        if (!isContentAssistConfigurationOkay()) {
            int offset = context.getInvocationOffset();
            EnableCompletionProposal config = new EnableCompletionProposal(images, offset);
            boolean hasOtherProposals = !crContext.getProposals().isEmpty();
            if (hasOtherProposals) {
                // Return the configure proposal
                return ImmutableList.<ICompletionProposal>of(config);
            } else {
                return ImmutableList.<ICompletionProposal>of(config, new EmptyCompletionProposal(offset));
            }
        } else {
            List<ICompletionProposal> res = Lists.newLinkedList();

            registerCompletionListener();

            fireStartSession(crContext);
            for (Entry<IJavaCompletionProposal, CompletionProposal> pair : crContext.getProposals().entrySet()) {
                IJavaCompletionProposal jdtProposal = create(pair.getValue(), pair.getKey(), jdtContext,
                        proposalFactory);
                res.add(jdtProposal);
                if (jdtProposal instanceof IProcessableProposal) {
                    IProcessableProposal crProposal = (IProcessableProposal) jdtProposal;
                    crProposal.setTag(CONTEXT, crContext);
                    crProposal.setTag(IS_VISIBLE, true);
                    crProposal.setTag(JDT_UI_PROPOSAL, pair.getKey());
                    crProposal.setTag(JDT_CORE_PROPOSAL, pair.getValue());
                    crProposal.setTag(JDT_SCORE, jdtProposal.getRelevance());
                    fireProcessProposal(crProposal);
                }
            }
            fireEndComputation(res);
            fireAboutToShow(res);

            return res;
        }
    }

    @Override
    public void sessionEnded() {
        fireAboutToClose();
    }

    private void storeContext(ContentAssistInvocationContext context) {
        jdtContext = cast(context);
        crContext = new RecommendersCompletionContext(jdtContext, astProvider, functions);
    }

    protected boolean isContentAssistConfigurationOkay() {
        Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (cats.contains(RECOMMENDERS_ALL_CATEGORY_ID)) {
            // If we are excluded on the default tab, then we cannot be on the default tab now, as we are executing.
            // Hence, we must be on a subsequent tab.
            return true;
        }
        if (isJdtAllEnabled(cats) || isMylynInstalledAndEnabled(cats)) {
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

    private boolean isMylynInstalled() {
        CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
        for (CompletionProposalCategory cat : reg.getProposalCategories()) {
            if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                return true;
            }
        }
        return false;
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

    protected void fireStartSession(IRecommendersCompletionContext crContext) {
        for (Iterator<SessionProcessor> it = activeProcessors.iterator(); it.hasNext();) {
            SessionProcessor p = it.next();
            try {
                boolean interested = p.startSession(crContext);
                if (!interested) {
                    it.remove();
                }
            } catch (Exception e) {
                Logs.log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
    }

    protected void fireProcessProposal(IProcessableProposal proposal) {
        for (SessionProcessor p : activeProcessors) {
            try {
                proposal.getRelevance();
                p.process(proposal);
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
        proposal.getProposalProcessorManager().prefixChanged(crContext.getPrefix());
    }

    protected void fireEndComputation(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : activeProcessors) {
            try {
                p.endSession(proposals);
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
    }

    protected void fireAboutToShow(List<ICompletionProposal> proposals) {
        for (SessionProcessor p : activeProcessors) {
            try {
                p.aboutToShow(proposals);
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
    }

    protected void fireAboutToClose() {
        for (SessionProcessor p : activeProcessors) {
            try {
                p.aboutToClose();
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
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

        // Calling unregister here seems like a good choice here but unfortunately isn't. "proposal applied" events are
        // fired after the sessionEnded event, and thus, we cannot use this method to unsubscribe from the current
        // editor. See unregisterCompletionListern for details.
    }

    @Override
    public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
        for (SessionProcessor p : activeProcessors) {
            try {
                p.selected(proposal);
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
    }

    @Override
    public void applied(ICompletionProposal proposal) {
        for (SessionProcessor p : activeProcessors) {
            try {
                p.applied(proposal);
            } catch (Exception e) {
                log(LOG_ERROR_SESSION_PROCESSOR_FAILED, e, p.getClass());
            }
        }
        unregisterCompletionListener();
    }
}
