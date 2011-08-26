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
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.JavaTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Transforms {@link PatternRecommendation}s into
 * {@link IJavaCompletionProposal}s which are applied on the editor content when
 * the proposal is selected from the completion proposals menu.
 */
@SuppressWarnings("restriction")
public final class CompletionProposalsBuilder {

    private static final int RELEVANCE_OFFSET = 570;
    private final Image templateIcon;
    private final CodeBuilder codeBuilder;

    /**
     * @param templateIcon
     *            The icon to be shown along with the completion proposals.
     * @param codeBuilder
     *            The {@link CodeBuilder} will turn MethodCalls into the code to
     *            be used by the Eclipse template engine.
     */
    public CompletionProposalsBuilder(final Image templateIcon, final CodeBuilder codeBuilder) {
        this.templateIcon = templateIcon;
        this.codeBuilder = Checks.ensureIsNotNull(codeBuilder);
    }

    /**
     * @param patterns
     *            The patterns which shall be turned into completion proposals.
     * @param context
     *            The context from which the completion request was invoked.
     * @param targetVariable
     *            The variable on which the methods proposed by the patterns
     *            shall be invoked.
     * @return A list of completion proposals for the given patterns.
     */
    public ImmutableList<? extends IJavaCompletionProposal> computeProposals(
            final Collection<PatternRecommendation> patterns, final DocumentTemplateContext context,
            final CompletionTargetVariable targetVariable) {
        final List<JavaTemplateProposal> proposals = Lists.newLinkedList();
        for (final PatternRecommendation pattern : patterns) {
            try {
                final JavaTemplateProposal template = buildTemplateProposal(pattern, context, targetVariable);
                proposals.add(template);
            } catch (final JavaModelException e) {
                continue;
            }
        }
        sortProposals(proposals);
        return ImmutableList.copyOf(proposals);
    }

    private void sortProposals(final List<JavaTemplateProposal> proposals) {
        Collections.sort(proposals);
        int i = RELEVANCE_OFFSET;
        for (final JavaTemplateProposal proposal : proposals) {
            proposal.setRelevance(i);
            i++;
        }
    }

    /**
     * @param patternRecommendation
     *            The pattern which shall be turned into a completion proposal.
     * @param context
     *            The context from which the completion request was invoked.
     * @param targetVariableName
     *            The name of the variable on which the methods proposed by the
     *            pattern shall be invoked.
     * @return The given pattern turned into a proposal object.
     */
    private JavaTemplateProposal buildTemplateProposal(final PatternRecommendation patternRecommendation,
            final DocumentTemplateContext context, final CompletionTargetVariable targetVariable)
            throws JavaModelException {
        final Template template = createTemplate(patternRecommendation, targetVariable);
        return new JavaTemplateProposal(template, context, templateIcon, patternRecommendation);
    }

    private Template createTemplate(final PatternRecommendation patternRecommendation,
            final CompletionTargetVariable targetVariable) throws JavaModelException {
        final String code = codeBuilder.buildCode(patternRecommendation.getMethods(), targetVariable);
        final String templateName = patternRecommendation.getName();
        final String templateDescription = patternRecommendation.getType().getClassName();
        final Template template = new Template(templateName, templateDescription, "java", code, false);
        return template;
    }
}
