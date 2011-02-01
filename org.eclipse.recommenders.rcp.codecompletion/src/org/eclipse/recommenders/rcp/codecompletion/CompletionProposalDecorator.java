/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.codecompletion;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A decorator for a standard java code completion proposal which additionally
 * displays the likelihood of the proposal.
 */
@SuppressWarnings("restriction")
public class CompletionProposalDecorator implements IJavaCompletionProposal, ICompletionProposalExtension6 {

    private final AbstractJavaCompletionProposal delegate;

    private final IRecommendation recommendation;

    public CompletionProposalDecorator(final IJavaCompletionProposal delegate, final IRecommendation recommendation) {
        ensureIsNotNull(delegate, "delegate");
        ensureIsNotNull(recommendation, "recommendation");
        this.recommendation = recommendation;
        this.delegate = (AbstractJavaCompletionProposal) delegate;
    }

    @Override
    public void apply(final IDocument document) {
        delegate.apply(document);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return delegate.getAdditionalProposalInfo();
    }

    @Override
    public IContextInformation getContextInformation() {
        return delegate.getContextInformation();
    }

    @Override
    public String getDisplayString() {
        return delegate.getDisplayString();
    }

    @Override
    public Image getImage() {
        return delegate.getImage();
    }

    @Override
    public int getRelevance() {
        final int rounded = computePercentage();
        return delegate.getRelevance() + rounded + 500;
    }

    private int computePercentage() {
        final double precentage = recommendation.getProbability() * 100;
        return (int) Math.rint(precentage);
    }

    @Override
    public Point getSelection(final IDocument document) {
        return delegate.getSelection(document);
    }

    @Override
    public StyledString getStyledDisplayString() {
        final int percentage = computePercentage();
        final StyledString origStyledString = delegate.getStyledDisplayString();
        if (origStyledString.toString().endsWith("%")) {
            // XXX why do we reenter the label decoration twice?
            // HACK
            // TODO check: do we still reenter this in 3.6?
            return origStyledString;
        }
        final StyledString appendix = new StyledString(" - " + percentage + " %", StyledString.COUNTER_STYLER);
        final StyledString res = origStyledString.append(appendix);
        return res;
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
