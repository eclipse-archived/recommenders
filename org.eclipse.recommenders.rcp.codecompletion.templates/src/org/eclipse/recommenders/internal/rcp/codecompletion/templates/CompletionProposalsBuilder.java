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

import com.google.common.collect.ImmutableList;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.JavaTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
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
    private final MethodCallFormatter methodCallFormatter;

    /**
     * @param templateIcon
     *            The icon to be shown along with the completion proposals.
     * @param methodCallFormatter
     *            The formatter which will turn the {@link MethodCall}s from the
     *            given {@link PatternRecommendation} into java code which will
     *            be inserted when the completion is selected.
     */
    public CompletionProposalsBuilder(final Image templateIcon, final MethodCallFormatter methodCallFormatter) {
        this.templateIcon = templateIcon;
        this.methodCallFormatter = methodCallFormatter;
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
        final List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
        for (final PatternRecommendation pattern : patterns) {
            proposals.add(buildTemplateProposal(pattern, context, completionTargetVariable));
        }
        return ImmutableList.copyOf(proposals);
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
        final String code = buildCode(patternRecommendation, completionTargetVariable);
        final String templateName = patternRecommendation.getName();
        final String templateDescription = completionTargetVariable.getType().getClassName();
        final Template template = new Template(templateName, templateDescription, "java", code, false);

        final Region region = new Region(context.getCompletionOffset(), context.getCompletionLength());
        final int probability = patternRecommendation.getProbability();
        return new JavaTemplateProposal(template, context, region, templateIcon, probability);
    }

    /**
     * @param patternRecommendation
     *            The pattern from which to take recommended method calls.
     * @param completionTargetVariable
     *            The variable on which the proposed methods shall be invoked.
     * @return The code to be inserted into the document, built from the
     *         recommended method calls and the given target variable.
     */
    private String buildCode(final PatternRecommendation patternRecommendation,
            final CompletionTargetVariable completionTargetVariable) {
        final StringBuilder code = new StringBuilder(32);
        final String lineSeparator = System.getProperty("line.separator");
        for (final IMethodName method : patternRecommendation.getMethods()) {
            try {
                code.append(methodCallFormatter.format(new MethodCall(completionTargetVariable, method)));
                code.append(lineSeparator);
            } catch (final JavaModelException e) {
                Throws.throwUnhandledException(e);
            }
        }
        methodCallFormatter.resetArgumentCounter();
        return String.format("%s${cursor}", code);
    }
}
