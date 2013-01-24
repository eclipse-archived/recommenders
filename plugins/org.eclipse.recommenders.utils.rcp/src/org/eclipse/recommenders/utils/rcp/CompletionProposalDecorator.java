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
package org.eclipse.recommenders.utils.rcp;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A decorator for a standard java code completion proposal which additionally displays the likelihood of the proposal.
 */
@SuppressWarnings("restriction")
public class CompletionProposalDecorator implements IJavaCompletionProposal, ICompletionProposalExtension,
        ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension5,
        ICompletionProposalExtension6 {

    private final AbstractJavaCompletionProposal delegate;
    private final double probablity;

    public CompletionProposalDecorator(final IJavaCompletionProposal delegate, final double probablity) {
        ensureIsNotNull(delegate, "delegate");
        this.delegate = (AbstractJavaCompletionProposal) delegate;
        this.probablity = probablity;
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
    public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
        return delegate.getAdditionalProposalInfo(monitor);
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

    private int computePercentage() {
        final double precentage = probablity * 100;
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

    @Override
    public int getRelevance() {
        return delegate.getRelevance();
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return delegate.getInformationControlCreator();
    }

    @Override
    public CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
        return delegate.getPrefixCompletionText(document, completionOffset);
    }

    @Override
    public int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
        return delegate.getPrefixCompletionStart(document, completionOffset);
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        delegate.apply(viewer, trigger, stateMask, offset);
    }

    @Override
    public void selected(final ITextViewer viewer, final boolean smartToggle) {
        delegate.selected(viewer, smartToggle);
    }

    @Override
    public void unselected(final ITextViewer viewer) {
        delegate.unselected(viewer);
    }

    @Override
    public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
        return delegate.validate(document, offset, event);
    }

    @Override
    public void apply(final IDocument document, final char trigger, final int offset) {
        delegate.apply(document, trigger, offset);
    }

    @Override
    public boolean isValidFor(final IDocument document, final int offset) {
        return delegate.isValidFor(document, offset);
    }

    @Override
    public char[] getTriggerCharacters() {
        return delegate.getTriggerCharacters();
    }

    @Override
    public int getContextInformationPosition() {
        return delegate.getContextInformationPosition();
    }
}
