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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Extends the {@link TemplateProposal} to customize the style of the template's
 * entry in the completion popup.
 */
@SuppressWarnings("restriction")
public final class JavaTemplateProposal extends TemplateProposal {

    private static final int RELEVANCE_OFFSET = 562;

    /**
     * Creates a template proposal with a template and its context.
     * 
     * @param template
     *            the template
     * @param context
     *            the context in which the template was requested.
     * @param region
     *            the region this proposal is applied to
     * @param image
     *            the icon of the proposal.
     * @param probability
     *            the probability that the represented pattern is applied in the
     *            given context.
     */
    public JavaTemplateProposal(final Template template, final TemplateContext context, final IRegion region,
            final Image image, final int probability) {
        super(template, context, region, image);
        setRelevance(RELEVANCE_OFFSET + probability);
        computeStyledDisplayString();
    }

    /**
     * Sets the string to be displayed in the completion proposals view. It
     * contains the template's name and its probability.
     */
    private void computeStyledDisplayString() {
        final Integer relevance = Integer.valueOf(getRelevance() - RELEVANCE_OFFSET);
        final StyledString styledString = new StyledString();
        styledString.append(String.format("dynamic '%s'", getTemplate().getDescription()));
        styledString.append(" - ", StyledString.QUALIFIER_STYLER);
        styledString.append(getTemplate().getName().replace("pattern", "Pattern #"), StyledString.QUALIFIER_STYLER);
        styledString.append(String.format(" - %d %%", relevance), StyledString.COUNTER_STYLER);
        setDisplayString(styledString);
    }
}
