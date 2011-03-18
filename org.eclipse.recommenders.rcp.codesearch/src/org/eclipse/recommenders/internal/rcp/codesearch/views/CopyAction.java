package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

final class CopyAction extends Action {
    /**
     * 
     */
    private final ResultsView resultsView;
    private final CodesearchController controller;

    /**
     * @param resultsView
     */
    CopyAction(final ResultsView resultsView, final CodesearchController controller) {
        this.resultsView = resultsView;
        this.controller = controller;
    }

    /* The control that currently has focus inside of this view */
    private Control focusControl;

    @Override
    public void run() {

        if (findFocusControl(this.resultsView.summariesContainer)) {
            sendTextToClipboard();
            sendTextCopiedFeedback();
        }

    }

    private void sendTextCopiedFeedback() {
        final RCPProposal proposal = findProposalForControl(focusControl);
        if (proposal == null) {
            return;
        }
        controller.sendFeedback(proposal, FeedbackType.TEXT_COPIED);
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
        final RCPProposal proposal = controller.lookupProposal(focus);
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
}