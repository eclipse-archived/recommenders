/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain.jdt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class basically delegates all events to a {@link TemplateProposal} but provides some auxilary methods for
 * testing such as {@link #getChain()}, and {@link #getChainElementNames()} etc. It may be extended to track user click
 * feedback to continuously improve chain completion.
 */
public class CallChainCompletionProposal implements IJavaCompletionProposal, ICompletionProposalExtension2 {

    private final List<MemberEdge> chain;
    private final TemplateProposal completion;

    public CallChainCompletionProposal(final List<MemberEdge> chain, final int invocationOffset) {
        this.chain = chain;
        completion = null;
    }

    public CallChainCompletionProposal(final TemplateProposal completion, final List<MemberEdge> chain) {
        this.completion = completion;
        this.chain = chain;
    }

    public List<MemberEdge> getChain() {
        return chain;
    }

    public List<String> getChainElementNames() {
        final List<String> b = new LinkedList<String>();
        for (final MemberEdge edge : chain) {
            b.add(edge.getEdgeElement().getElementName());
        }
        return b;
    }

    @Override
    public void apply(final IDocument document) {
        completion.apply(document);
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        completion.apply(viewer, trigger, stateMask, offset);

    }

    @Override
    public String getAdditionalProposalInfo() {
        return completion.getAdditionalProposalInfo();
    }

    @Override
    public String getDisplayString() {
        return completion.getDisplayString();

    }

    @Override
    public IContextInformation getContextInformation() {
        return completion.getContextInformation();

    }

    @Override
    public int getRelevance() {
        final int relevance = (2 ^ 12) - chain.size();
        return relevance;
    }

    @Override
    public Point getSelection(final IDocument document) {
        return completion.getSelection(document);
    }

    @Override
    public Image getImage() {
        return completion.getImage();
    }

    @Override
    public void selected(final ITextViewer viewer, final boolean smartToggle) {
        completion.selected(viewer, smartToggle);

    }

    @Override
    public void unselected(final ITextViewer viewer) {
        completion.unselected(viewer);
    }

    @Override
    public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
        return completion.validate(document, offset, event);
    }

    @Override
    public String toString() {
        return completion.getDisplayString();
    }
}
