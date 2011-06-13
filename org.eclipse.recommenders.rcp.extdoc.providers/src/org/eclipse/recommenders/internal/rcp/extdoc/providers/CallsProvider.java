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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class CallsProvider extends AbstractProviderComposite {

    private final CallsCompletionProposalComputer proposalComputer;
    private final CallsServer server = new CallsServer();

    private StyledText styledText;
    private Composite patterns;
    private Composite composite;
    private FeaturesComposite features;

    @Inject
    public CallsProvider(final CallsCompletionProposalComputer proposalComputer,
            final IntelligentCompletionContextResolver contextResolver) {
        this.proposalComputer = proposalComputer;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        styledText = SwtFactory.createStyledText(composite, "");
        return composite;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        // TODO: IMethod is just for testing.
        if ((element instanceof IType || element instanceof IField || element instanceof ILocalVariable || element instanceof IMethod)
                && selection.getInvocationContext() != null) {
            @SuppressWarnings("unchecked")
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
        styledText.setText("By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:");
        SwtFactory.createStyleRange(styledText, 30, element.getElementName().length(), SWT.NORMAL, false, true);

        disposePatterns();
        patterns = SwtFactory.createGridComposite(composite, 3, 12, 3, 12, 0);

        for (final IJavaCompletionProposal proposal : proposals) {
            SwtFactory.createSquare(patterns);
            SwtFactory.createLabel(patterns, proposal.getDisplayString(), false, false, false);
            SwtFactory.createLabel(patterns, (proposal.getRelevance() - 1075) + "%", false, true, false);
        }

        features = FeaturesComposite.create(composite, element, element.getElementName(), this, server,
                new TemplateEditDialog(getShell()));
        composite.layout(true);
    }

    private void disposePatterns() {
        if (patterns != null) {
            patterns.dispose();
            features.dispose();
        }
    }
}
