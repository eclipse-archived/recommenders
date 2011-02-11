/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Extends the <code>TemplateProposal</code> to customize the style of the
 * template's entry in the completion popup.
 */
@SuppressWarnings("restriction")
public final class JavaTemplateProposal extends TemplateProposal {

    private static IDocument currentDocument;
    private static final int RELEVANCE_OFFSET = 562;

    public JavaTemplateProposal(final Template template, final TemplateContext context, final IRegion region,
            final Image image, final int probability) {
        super(template, context, region, image);
        setRelevance(RELEVANCE_OFFSET + probability);
        computeStyledDisplayString();
    }

    private void computeStyledDisplayString() {
        final StyledString styledString = new StyledString();
        styledString.append(String.format("dynamic '%s'", getTemplate().getDescription()));
        styledString.append(" - ", StyledString.QUALIFIER_STYLER);
        styledString.append(getTemplate().getName().replace("pattern", "Pattern #"), StyledString.QUALIFIER_STYLER);
        styledString.append(String.format(" - %d %%", Integer.valueOf(getRelevance() - RELEVANCE_OFFSET)),
                StyledString.COUNTER_STYLER);
        setDisplayString(styledString);
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        JavaTemplateProposal.setCurrentDocument(viewer.getDocument());
        super.apply(viewer, trigger, stateMask, offset);
    }

    private static void setCurrentDocument(final IDocument document) {
        JavaTemplateProposal.currentDocument = document;
    }

    static IDocument getCurrentDocument() {
        return JavaTemplateProposal.currentDocument;
    }
}
