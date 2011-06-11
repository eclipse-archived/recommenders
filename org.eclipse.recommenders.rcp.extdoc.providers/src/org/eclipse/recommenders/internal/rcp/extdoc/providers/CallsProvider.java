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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected Control createContentControl(final Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 11;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        styledText = new StyledText(composite, SWT.WRAP);
        styledText.setEnabled(false);
        styledText.setDoubleClickEnabled(false);
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        styledText.setEditable(false);

        return composite;
    }

    @Override
    protected void updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        // TODO: IMethod is just for testing.
        if ((element instanceof IType || element instanceof IField || element instanceof ILocalVariable || element instanceof IMethod)
                && selection.getInvocationContext() != null) {
            @SuppressWarnings("unchecked")
            final List<IJavaCompletionProposal> proposals = proposalComputer.computeCompletionProposals(
                    selection.getInvocationContext(), null);
            if (!proposals.isEmpty()) {
                printProposals(element, proposals);
            } else {
                printNoneAvailable(element.getElementName());
            }
        } else {
            printUnavailable();
        }
    }

    private void printProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
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

        features = new FeaturesComposite(composite);
        features.addCommentsIcon(element, element.getElementName(), this);
        features.addEditIcon(new TemplateEditDialog(getShell()));
        features.addStarsRating(element, server);

        composite.layout(true);
    }

    private void printNoneAvailable(final String elementName) {
        styledText.setText("There are no method calls available for " + elementName + ".");
        SwtFactory.createStyleRange(styledText, 40, elementName.length(), SWT.NORMAL, false, true);
        disposePatterns();
    }

    private void printUnavailable() {
        styledText.setText("Method calls are only available for Java types and variables.");
        disposePatterns();
    }

    private void disposePatterns() {
        if (patterns != null) {
            patterns.dispose();
            features.dispose();
        }
    }
}
