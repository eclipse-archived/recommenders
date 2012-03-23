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
package org.eclipse.recommenders.internal.completion.rcp.templates.types;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Extends the {@link TemplateProposal} to customize the style of the template's entry in the completion popup.
 */
@SuppressWarnings("restriction")
public final class JavaTemplateProposal extends TemplateProposal implements Comparable<JavaTemplateProposal> {

    private final PatternRecommendation patternRecommendation;

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
     * @param patternRecommendation
     * @param targetVariable
     * @param probability
     *            the probability that the represented pattern is applied in the given context.
     * @param isExactNameMatch
     */
    public JavaTemplateProposal(final Template template, final DocumentTemplateContext context, final Image image,
            final PatternRecommendation patternRecommendation) {
        super(template, context, calculateReplacementRegion(context), image);
        this.patternRecommendation = patternRecommendation;
        computeStyledDisplayString();
        setRelevance((int) (getRelevance() + Math.round(patternRecommendation.getProbability() * 100)));
    }

    private static IRegion calculateReplacementRegion(final DocumentTemplateContext context) {
        return new Region(context.getCompletionOffset(), context.getCompletionLength());
    }

    /**
     * Sets the string to be displayed in the completion proposals view. It contains the template's name and its
     * probability.
     */
    private void computeStyledDisplayString() {
        final StyledString styledString = new StyledString();
        styledString.append(String.format("dynamic '%s'", getTemplate().getDescription()));
        styledString.append(" - ", StyledString.QUALIFIER_STYLER);
        styledString.append(getTemplate().getName().replace("pattern", "Pattern #"), StyledString.QUALIFIER_STYLER);
        styledString.append(String.format(" - %d %%", Math.round(patternRecommendation.getProbability() * 100)),
                StyledString.COUNTER_STYLER);
        setDisplayString(styledString);
    }

    @Override
    public int compareTo(final JavaTemplateProposal o) {
        final int compareClassName = compareByClassName(o);
        if (compareClassName == 0) {
            return compareByProbability(o);
        } else {
            return -compareClassName;
        }
    }

    private int compareByProbability(final JavaTemplateProposal o) {
        return Double.valueOf(patternRecommendation.getProbability()).compareTo(
                o.patternRecommendation.getProbability());
    }

    private int compareByClassName(final JavaTemplateProposal o) {
        return patternRecommendation.getType().getClassName()
                .compareTo(o.patternRecommendation.getType().getClassName());
    }

    @Override
    public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
        try {
            final int replaceOffset = getReplaceOffset();
            if (offset >= replaceOffset) {
                final String content = document.get(replaceOffset, offset - replaceOffset);
                final String className = patternRecommendation.getType().getClassName();

                return StringUtils.startsWithIgnoreCase(className, content);
            }
        } catch (final BadLocationException e) {
            // concurrent modification - ignore
        }
        return false;
    }

}
