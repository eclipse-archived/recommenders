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
package org.eclipse.recommenders.internal.calls.rcp.templates;

import static org.eclipse.recommenders.utils.Recommendations.asPercentage;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;

/**
 * Extends the {@link TemplateProposal} to customize the style of the template's entry in the completion popup.
 */
@SuppressWarnings("restriction")
public class JavaTemplateProposal extends TemplateProposal implements Comparable<JavaTemplateProposal> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private PatternRecommendation recommendation;

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
     */
    public JavaTemplateProposal(Template template, DocumentTemplateContext context, Image image,
            PatternRecommendation recommendation) {
        super(template, context, calculateReplacementRegion(context), image);
        this.recommendation = recommendation;
        computeStyledDisplayString();
        setRelevance(getRelevance() + asPercentage(recommendation));
    }

    private static IRegion calculateReplacementRegion(DocumentTemplateContext context) {
        return new Region(context.getCompletionOffset(), context.getCompletionLength());
    }

    /**
     * Sets the string to be displayed in the completion proposals view. It contains the template's name and its
     * probability.
     */
    private void computeStyledDisplayString() {
        StyledString styledString = new StyledString();
        styledString.append(String.format("dynamic '%s'", getTemplate().getDescription()));
        styledString.append(" - ", StyledString.QUALIFIER_STYLER);
        styledString.append(getTemplate().getName().replace("pattern", "Pattern #"), StyledString.QUALIFIER_STYLER);
        styledString.append(String.format(" - %d %%", asPercentage(recommendation)), StyledString.COUNTER_STYLER);
        setDisplayString(styledString);
    }

    @Override
    public int compareTo(JavaTemplateProposal o) {
        return ComparisonChain.start()
                .compare(recommendation.getType().getClassName(), o.recommendation.getType().getClassName())
                .compare(recommendation.getRelevance(), o.recommendation.getRelevance()).result();
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        try {
            int replaceOffset = getReplaceOffset();
            if (offset >= replaceOffset) {
                String content = document.get(replaceOffset, offset - replaceOffset);
                String className = recommendation.getType().getClassName();
                return StringUtils.startsWithIgnoreCase(className, content);
            }
        } catch (BadLocationException e) {
            log.error("Failed to validate template", e);
        }
        return false;
    }

}
