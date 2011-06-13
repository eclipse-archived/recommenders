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
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CompletionInvocationContext;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.TemplatesServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class TemplatesProvider extends AbstractProviderComposite implements IDeletionProvider {

    private final TemplatesCompletionProposalComputer proposalComputer;
    private final IntelligentCompletionContextResolver contextResolver;
    private final TemplatesServer server;

    private Composite composite;
    private StyledText styledText;
    private Composite templates;

    @Inject
    public TemplatesProvider(final TemplatesCompletionProposalComputer proposalComputer,
            final IntelligentCompletionContextResolver contextResolver) {
        this.proposalComputer = proposalComputer;
        this.contextResolver = contextResolver;
        server = new TemplatesServer();
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        styledText = SwtFactory.createStyledText(composite, "");
        return composite;
    }

    @Override
    protected void updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        // TODO: IMethod is just for testing.
        if ((element instanceof IType || element instanceof IField || element instanceof ILocalVariable || element instanceof IMethod)
                && selection.getInvocationContext() != null) {
            final CompletionInvocationContext context = new CompletionInvocationContext(
                    selection.getInvocationContext(), selection.getEditor());
            final IIntelligentCompletionContext completionContext = contextResolver.resolveContext(context);
            final List<IJavaCompletionProposal> proposals = proposalComputer
                    .computeCompletionProposals(completionContext);
            if (proposals.isEmpty()) {
                displayNoneAvailable(element.getElementName());
            } else {
                displayProposals(element, proposals);
            }
        } else {
            printUnavailable();
        }
    }

    private void displayProposals(final IJavaElement element, final List<IJavaCompletionProposal> proposals) {
        styledText.setText("By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:");

        disposeTemplates();
        templates = SwtFactory.createGridComposite(composite, 1, 0, 12, 0, 0);

        for (final IJavaCompletionProposal proposal : proposals) {
            final Composite editLine = SwtFactory.createGridComposite(templates, 2, 10, 0, 0, 0);
            SwtFactory.createLabel(editLine, proposal.getDisplayString(), false, false, false);
            FeaturesComposite.create(editLine, element, element.getElementName(), this, server, new TemplateEditDialog(
                    getShell()));

            final Composite template = SwtFactory.createGridComposite(templates, 1, 12, 3, 12, 0);
            for (final String line : proposal.getAdditionalProposalInfo().split(";\r?\n")) {
                SwtFactory.createLabel(template, line, false, false, true);
            }
        }
        composite.layout(true);
    }

    private void displayNoneAvailable(final String elementName) {
        styledText.setText("There are no templates available for " + elementName + ".");
        SwtFactory.createStyleRange(styledText, 37, elementName.length(), SWT.NORMAL, false, true);
        disposeTemplates();
    }

    private void printUnavailable() {
        styledText.setText("Templates are only available for Java types and variables.");
        disposeTemplates();
    }

    private void disposeTemplates() {
        if (templates != null) {
            templates.dispose();
        }
    }

    @Override
    public void requestDeletion(final Object object) {
        // TODO Auto-generated method stub
    }

}
