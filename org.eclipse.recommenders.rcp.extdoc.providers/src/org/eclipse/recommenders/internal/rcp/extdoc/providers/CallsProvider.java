/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import java.util.List;

import com.google.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class CallsProvider extends AbstractProviderComposite {

    private final CallsCompletionProposalComputer proposalComputer;
    private final CallsServer server = new CallsServer();

    private Composite composite;
    private Composite calls;
    private TextAndFeaturesLine line;

    @Inject
    public CallsProvider(final CallsCompletionProposalComputer proposalComputer) {
        this.proposalComputer = proposalComputer;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (selection.getInvocationContext() != null) {
            final List<IJavaCompletionProposal> proposals = proposalComputer.computeCompletionProposals(
                    selection.getInvocationContext(), null);
            if (!proposals.isEmpty()) {
                displayProposals(element, proposals);
                return true;
            }
        }
        return false;
    }

    private void displayProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
        if (calls != null) {
            line.dispose();
            calls.dispose();
        }

        final String text = "By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:";
        line = new TextAndFeaturesLine(composite, text, element, element.getElementName(), this, server,
                new TemplateEditDialog(getShell()));
        line.createStyleRange(30, element.getElementName().length(), SWT.NORMAL, false, true);

        calls = SwtFactory.createGridComposite(composite, 3, 12, 3, 12, 0);
        for (final IJavaCompletionProposal proposal : proposals) {
            SwtFactory.createSquare(calls);
            SwtFactory.createLabel(calls, proposal.getDisplayString(), false, false, false);
            SwtFactory.createLabel(calls, (proposal.getRelevance() - 1075) + "%", false, true, false);
        }

        composite.layout(true);
    }
}
