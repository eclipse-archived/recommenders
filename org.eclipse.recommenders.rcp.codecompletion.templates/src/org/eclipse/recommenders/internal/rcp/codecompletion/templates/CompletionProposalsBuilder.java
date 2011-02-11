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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.Expression;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.JavaTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.swt.graphics.Image;

/**
 * Transforms {@link PatternRecommendation}s into
 * {@link IJavaCompletionProposal}s which can be passed to Eclipse.
 */
@SuppressWarnings("restriction")
final class CompletionProposalsBuilder {

    private final Image templateIcon;
    private final ExpressionFormatter expressionFormatter;

    /**
     * @param templateIcon
     *            The icon to be shown along with the proposal.
     * @param expressionFormatter
     *            The formatter which will turn the {@link Expression}s inside
     *            the {@link PatternRecommendation} into the code which will be
     *            inserted when the completion is selected.
     */
    CompletionProposalsBuilder(final Image templateIcon, final ExpressionFormatter expressionFormatter) {
        this.templateIcon = templateIcon;
        this.expressionFormatter = expressionFormatter;
    }

    public List<IJavaCompletionProposal> computeProposals(final Collection<PatternRecommendation> patterns,
            final DocumentTemplateContext context, final CompletionTargetVariable completionTargetVariable) {
        final List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
        for (final PatternRecommendation pattern : patterns) {
            proposals.add(buildTemplateProposal(pattern, context, completionTargetVariable));
        }
        return proposals;
    }

    private TemplateProposal buildTemplateProposal(final PatternRecommendation patternRecommendation,
            final DocumentTemplateContext context, final CompletionTargetVariable completionTargetVariable) {
        final String code = buildCode(patternRecommendation, completionTargetVariable);
        final String templateName = patternRecommendation.getName();
        final String templateDescription = completionTargetVariable.getType().getClassName();
        final Template template = new Template(templateName, templateDescription, "java", code, false);

        final Region region = new Region(context.getCompletionOffset(), context.getCompletionLength());
        return new JavaTemplateProposal(template, context, region, templateIcon, patternRecommendation.getProbability());
    }

    private String buildCode(final PatternRecommendation usagePattern,
            final CompletionTargetVariable completionTargetVariable) {
        final StringBuilder code = new StringBuilder(32);
        final String lineSeparator = System.getProperty("line.separator");
        for (final IMethodName method : usagePattern.getMethods()) {
            try {
                final Expression expression = new Expression(completionTargetVariable, method);
                code.append(String.format("%s%s", expressionFormatter.format(expression), lineSeparator));
            } catch (final JavaModelException e) {
                Throws.throwUnhandledException(e);
            }
        }
        expressionFormatter.resetArgumentCounter();
        return String.format("%s${cursor}", code);
    }
}
