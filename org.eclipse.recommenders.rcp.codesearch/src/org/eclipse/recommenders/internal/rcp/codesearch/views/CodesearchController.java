/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class CodesearchController {

    private BiMap<Control, RCPProposal> controls2ProposalsIndex = HashBiMap.create();

    private final CodeSearchClient client;
    private int showedProposalIndex;

    private RCPResponse response;

    @Inject
    public CodesearchController(final CodeSearchClient client) {
        this.client = client;
    }

    public void openInEditor(final RCPProposal proposal) {
        new OpenSourceCodeInEditorJob(response.getRequest().query, proposal, this).schedule();
    }

    public void sendFeedback(final RCPProposal proposal, final FeedbackType feedbackType) {
        new WorkspaceJob("Sending User Feedback...") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                client.addFeedback(response.getRequestId(), Feedback.newFeedback(proposal.getId(), feedbackType));
                return Status.OK_STATUS;
            }
        }.schedule();
        // System.out.printf("send feedback: %s %s %s\n", feedbackType,
        // response.getRequestId(), proposal.getId());
    }

    public synchronized void sendRequest(final Request request, final IJavaProject issuingProject) {
        uiClearResultsView();
        showedProposalIndex = 0;
        controls2ProposalsIndex.clear();
        final WorkspaceJob job = new WorkspaceJob("Searching Code Examples...") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                final Response serverResponse = client.search(request);
                response = RCPResponse.newInstance(request, serverResponse, issuingProject);
                return Status.OK_STATUS;
            }
        };
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    updateQueryView(response, issuingProject);
                    showNextProposals(7);
                }
            }

        });
        job.schedule();
    }

    public void showNextProposals(final int count) {
        final List<RCPProposal> nextProposals = computeNextBlock(count);

        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                uiRemoveMoreBlock();
                uiAddLoadingInProgressBlock();
                uiUpdateView();
            }
        });
        bgFetchAndParseSources(nextProposals);

    }

    public ResultsView showResultsView() {
        return CodesearchPlugin.showExamplesView();
    }

    private QueryView uiShowQueryView() {
        return CodesearchPlugin.showQueryView();
    }

    private void updateQueryView(final RCPResponse response, final IJavaProject issuingProject) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                uiShowQueryView().setInput(response.getRequest(), issuingProject);
            }
        });
    }

    public void removeProposal(final RCPProposal proposal) {
        uiRemoveProposalFromViewAndIndex(proposal);
        showNextProposals(1);
    }

    public RCPResponse getResponse() {
        return response;
    }

    private void uiUpdateView() {
        final ResultsView resultsView = showResultsView();
        resultsView.update();
    }

    private void uiRemoveLoadingInProgressBlock() {
        uiRemoveProposalFromViewAndIndex(LoadingInProgressBlock.LOADING_IN_PROGRESS);
    }

    private void uiAddLoadingInProgressBlock() {
        uiAddToViewAndIndex(new LoadingInProgressBlock(), LoadingInProgressBlock.LOADING_IN_PROGRESS);
    }

    private void uiAddToViewAndIndex(final ICodeSummaryBlock block, final RCPProposal proposal) {
        if (!controls2ProposalsIndex.containsValue(proposal)) {
            final Control control = block.createControl(uiGetSummaryArea());
            block.display(response, proposal);
            controls2ProposalsIndex.put(control, proposal);
        }
    }

    private List<RCPProposal> computeNextBlock(final int count) {
        final int nextStop = computeNextStopIndex(count);
        final List<RCPProposal> proposals = response.getProposals();
        final List<RCPProposal> res = proposals.subList(showedProposalIndex, nextStop);
        return res;
    }

    private int computeNextStopIndex(final int count) {
        final int nextStop = showedProposalIndex + count;
        final int maxElementsAvailable = response.getNumberOfProposals();
        return Math.min(nextStop, maxElementsAvailable);

    }

    private void bgFetchAndParseSources(final List<RCPProposal> block) {
        new WorkspaceJob("Fetching Sources") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("Loading...", block.size());
                for (final RCPProposal proposal : block) {
                    proposal.getAst(new SubProgressMonitor(monitor, 1));
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            uiRemoveMoreBlock();
                            uiRemoveLoadingInProgressBlock();
                            incProposalIndex();
                            final ICodeSummaryBlock block = new StatmentsBasedCodeSummaryBlock(
                                    CodesearchController.this, new SummaryCodeFormatter());
                            uiAddToViewAndIndex(block, proposal);
                            uiAddLoadingInProgressBlock();
                            uiUpdateView();
                        }
                    });
                }
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        uiRemoveLoadingInProgressBlock();
                        if (hasMoreProposals()) {
                            uiAddMoreBlock();
                        } else {
                            uiAddEndOfProposalsBlockIfNotThere();
                        }
                        uiUpdateView();
                    }
                });
                return Status.OK_STATUS;
            }
        }.schedule();

    }

    private boolean hasMoreProposals() {
        return showedProposalIndex + 1 < response.getNumberOfProposals();
    }

    private void incProposalIndex() {
        showedProposalIndex++;
    }

    private void uiAddEndOfProposalsBlockIfNotThere() {
        uiAddToViewAndIndex(new EndOfResultsBlock(), EndOfResultsBlock.END_OF_RESULTS);
    }

    private void uiAddMoreBlock() {
        uiAddToViewAndIndex(new FindMoreBlock(this), FindMoreBlock.MORE);
    }

    private void uiClearResultsView() {
        Display.getCurrent().syncExec(new Runnable() {

            @Override
            public void run() {
                for (final RCPProposal p : Sets.newHashSet(controls2ProposalsIndex.values())) {
                    uiRemoveProposalFromViewAndIndex(p);
                }
                showResultsView().update();
            }
        });
    }

    private Composite uiGetSummaryArea() {
        final ResultsView resultsView = showResultsView();
        final Composite summaryArea = resultsView.getSummaryArea();
        return summaryArea;
    }

    private void uiRemoveMoreBlock() {
        uiRemoveProposalFromViewAndIndex(FindMoreBlock.MORE);
    }

    private void uiRemoveProposalFromViewAndIndex(final RCPProposal proposal) {
        final Control control = controls2ProposalsIndex.inverse().remove(proposal);
        if (control != null) {
            control.dispose();
        }

    }

    public RCPProposal lookupProposal(final Control control) {
        return controls2ProposalsIndex.get(control);
    }
}
