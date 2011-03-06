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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.JavaTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.swt.graphics.Image;

/**
 * Transforms {@link PatternRecommendation}s into
 * {@link IJavaCompletionProposal}s which are applied on the editor content when
 * the propoals is selected from the completion proposals menu.
 */
@SuppressWarnings("restriction")
public final class CompletionProposalsBuilder {

    private final Image templateIcon;
    private final CodeBuilder codeBuilder;

    /**
     * @param templateIcon
     *            The icon to be shown along with the completion proposals.
     */
    public CompletionProposalsBuilder(final Image templateIcon, final CodeBuilder codeBuilder) {
        this.templateIcon = templateIcon;
        this.codeBuilder = codeBuilder;
    }

    /**
     * @param patterns
     *            The patterns which shall be turned into completion proposals.
     * @param context
     *            The context from where the completion request was invoked.
     * @param completionTargetVariable
     *            The variable on which the completion request was invoked.
     * @return A list of completion proposals for the given patterns.
     */
    public ImmutableList<IJavaCompletionProposal> computeProposals(final Collection<PatternRecommendation> patterns,
            final DocumentTemplateContext context, final CompletionTargetVariable completionTargetVariable) {
        final Builder<IJavaCompletionProposal> proposals = ImmutableList.builder();
        for (final PatternRecommendation pattern : patterns) {
            proposals.add(buildTemplateProposal(pattern, context, completionTargetVariable));
        }
        return proposals.build();
    }

    /**
     * @param patternRecommendation
     *            The pattern which shall be turned into a completion proposal.
     * @param context
     *            The context from where the completion request was invoked.
     * @param completionTargetVariable
     *            The variable on which the completion request was invoked.
     * @return The given pattern turned into a proposal object.
     */
    private TemplateProposal buildTemplateProposal(final PatternRecommendation patternRecommendation,
            final DocumentTemplateContext context, final CompletionTargetVariable completionTargetVariable) {
        final String code = codeBuilder.buildCode(patternRecommendation.getMethods(), completionTargetVariable);
        final String templateName = patternRecommendation.getName();
        final String templateDescription = completionTargetVariable.getType().getClassName();
        final Template template = new Template(templateName, templateDescription, "java", code, false);

        final Region region = new Region(context.getCompletionOffset(), context.getCompletionLength());
        final int probability = patternRecommendation.getProbability();
        return new JavaTemplateProposal(template, context, region, templateIcon, probability);
    }
}
