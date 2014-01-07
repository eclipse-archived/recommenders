/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.jface.viewers.StyledString.QUALIFIER_STYLER;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
// leave a bit space for other, maybe more important proposals
// don't do anything. In particular do not call the super implementation.

@SuppressWarnings("restriction")
public class EmptyCompletionProposal extends AbstractJavaCompletionProposal {

    // leave a bit space for other, maybe more important proposals
    private static final int RELEVANCE = Integer.MAX_VALUE - 9000;

    public EmptyCompletionProposal(int invocationOffset) {
        StyledString text = new StyledString("no proposals", QUALIFIER_STYLER);
        setStyledDisplayString(text);
        setRelevance(RELEVANCE);
        setSortString(text.getString());
        setCursorPosition(invocationOffset);
        setReplacementString("");
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        // don't do anything. In particular do not call the super implementation.
    }

    @Override
    protected boolean isValidPrefix(String prefix) {
        return true;
    }
}
