/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendUserClickFeedbackJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;

public class ResultsView extends ViewPart {
    public static final String ID = ResultsView.class.getName();
    private ScrolledComposite rootContainer;
    private Composite summariesContainer;
    private RCPResponse response;
    private final CodeSearchClient searchClient;
    private Composite rootcontrol;
    private BiMap<Control, RCPProposal> summaryControl2ProposalIndex;

    @Inject
    public ResultsView(final CodeSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @Override
    public void createPartControl(final Composite parent) {
        rootContainer = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        rootContainer.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        rootContainer.setExpandHorizontal(true);
        rootContainer.setExpandVertical(true);
        rootContainer.setLayout(GridLayoutFactory.fillDefaults().create());
        rootContainer.setLayoutData(GridDataFactory.fillDefaults().create());

        // Speed up scrolling when using a wheel mouse
        final ScrollBar vBar = rootContainer.getVerticalBar();
        vBar.setIncrement(10);

        summariesContainer = new Composite(rootContainer, SWT.NONE);
        summariesContainer.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        summariesContainer.setLayout(GridLayoutFactory.fillDefaults().create());
        summariesContainer.setLayoutData(GridDataFactory.fillDefaults().create());
        rootContainer.setContent(summariesContainer);

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new Action() {
            /* The control that currently has focus inside of this view */
            private Control focusControl;

            @Override
            public void run() {

                if (findFocusControl(summariesContainer)) {
                    sendTextToClipboard();
                    sendTextCopiedFeedback();
                }

            }

            private void sendTextCopiedFeedback() {
                final RCPProposal proposal = findProposalForControl(focusControl);
                if (proposal == null) {
                    return;
                }
                new SendUserClickFeedbackJob(response.getRequestId(), Feedback.newFeedback(proposal.getId(),
                        FeedbackType.TEXT_COPIED), searchClient).schedule();
            }

            private void sendTextToClipboard() {
                if (focusControl instanceof StyledText) {
                    final StyledText widget = (StyledText) focusControl;
                    widget.copy();
                }
            }

            private RCPProposal findProposalForControl(final Control focus) {
                if (focus == null) {
                    return null;
                }
                final RCPProposal proposal = summaryControl2ProposalIndex.get(focus);
                if (proposal != null) {
                    return proposal;
                }
                return findProposalForControl(focus.getParent());
            }

            private boolean findFocusControl(final Control control) {
                focusControl = recursiveFindFocusControl(control);
                return focusControl != null;
            }

            private Control recursiveFindFocusControl(final Control control) {
                if (control.isFocusControl()) {
                    return control;
                }
                if (control instanceof Composite) {
                    for (final Control child : ((Composite) control).getChildren()) {
                        final Control focus = recursiveFindFocusControl(child);
                        if (focus != null) {
                            return focus;
                        }
                    }
                }
                // nothing found with focus?
                return null;
            }
        });
    }

    public void setInput(final RCPResponse reply) {
        this.response = reply;
        disposeOldSourceViewers();
        createNewSourceViewers();
    }

    private void disposeOldSourceViewers() {
        for (final Control child : summariesContainer.getChildren()) {
            child.dispose();
        }
    }

    private void createNewSourceViewers() {
        summaryControl2ProposalIndex = HashBiMap.create();
        for (final RCPProposal proposal : response.getProposals()) {
            final CodeSummaryPage page = new RelatedStatementsSummaryPage(searchClient);
            page.createControl(summariesContainer);
            page.setInput(response, proposal);
            summaryControl2ProposalIndex.put(page.getControl(), proposal);
        }
        final Point preferredSize = summariesContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        rootContainer.setMinSize(preferredSize);
        rootContainer.layout(true, true);
    }

    @Override
    public void setFocus() {
        summariesContainer.setFocus();
    }
}
