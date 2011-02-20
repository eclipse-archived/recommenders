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

import com.google.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.ModifiedJavaContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Controls the process of template recommendations.
 */
@SuppressWarnings("restriction")
final class TemplatesCompletionEngine implements IJavaCompletionProposalComputer {

    private final IntelligentCompletionContextResolver contextResolver;
    private final PatternRecommender patternRecommender;
    private CompletionProposalsBuilder completionProposalsBuilder;
    private AbstractJavaContextType templateContextType;

    /**
     * @param patternRecommender
     *            Computes and returns patterns suitable for the active
     *            completion request.
     * @param methodCallFormatter
     *            The formatter will turn MethodCalls into the code to be used
     *            by the eclipse template engine.
     * @param contextResolver
     *            Responsible for computing an
     *            {@link IIntelligentCompletionContext} from a
     *            {@link ContentAssistInvocationContext} which is given by the
     *            editor for each completion request.
     */
    @Inject
    TemplatesCompletionEngine(final PatternRecommender patternRecommender,
            final MethodCallFormatter methodCallFormatter, final IntelligentCompletionContextResolver contextResolver) {
        this.patternRecommender = patternRecommender;
        this.contextResolver = contextResolver;
        initializeProposalBuilder(methodCallFormatter);
        initializeTemplateContextType();
    }

    /**
     * Initializes the proposal builder.
     * 
     * @param methodCallFormatter
     *            The <code>ExpressionPrinter</code> to be used by the
     *            <code>ProposalsBuilder</code>.
     */
    private void initializeProposalBuilder(final MethodCallFormatter methodCallFormatter) {
        final Bundle bundle = FrameworkUtil.getBundle(TemplatesCompletionEngine.class);
        final Image icon = bundle == null ? null : AbstractUIPlugin.imageDescriptorFromPlugin(bundle.getSymbolicName(),
                "metadata/icon2.gif").createImage();
        completionProposalsBuilder = new CompletionProposalsBuilder(icon, methodCallFormatter);
    }

    /**
     * Sets the appropriate <code>ContextType</code> for all computed templates.
     */
    private void initializeTemplateContextType() {
        final JavaPlugin plugin = JavaPlugin.getDefault();
        templateContextType = plugin == null ? null : (AbstractJavaContextType) plugin.getTemplateContextRegistry()
                .getContextType(JavaContextType.ID_ALL);
    }

    @Override
    public List<IJavaCompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
        if (contextResolver.hasProjectRecommendersNature(jCtx)) {
            return computeProposals(contextResolver.resolveContext(jCtx));
        }
        return Collections.emptyList();
    }

    /**
     * @param context
     *            The context from where the completion request was invoked.
     * @return The completion proposals to be displayed in the editor.
     */
    public List<IJavaCompletionProposal> computeProposals(final IIntelligentCompletionContext context) {
        if (context.getEnclosingMethod() != null && (context.expectsReturnValue() || context.getExpectedType() == null)) {
            final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                    .createInvokedVariable(context);
            if (completionTargetVariable != null) {
                final Collection<PatternRecommendation> patternRecommendations = patternRecommender
                        .computeRecommendations(completionTargetVariable, context);
                final List<IJavaCompletionProposal> completionProposals = buildProposalsForPatterns(
                        patternRecommendations, completionTargetVariable, context);
                return completionProposals;
            }
        }
        return Collections.emptyList();
    }

    /**
     * @param patternRecommendations
     *            The recommendations computed by the {@link PatternRecommender}
     *            .
     * @param completionTargetVariable
     *            The variable on which the completion request was executed.
     * @param context
     *            The context from where the completion request was invoked.
     * @return The completion proposals to be displayed in the editor.
     */
    private List<IJavaCompletionProposal> buildProposalsForPatterns(
            final Collection<PatternRecommendation> patternRecommendations,
            final CompletionTargetVariable completionTargetVariable, final IIntelligentCompletionContext context) {
        List<IJavaCompletionProposal> completionProposals = Collections.emptyList();
        if (!patternRecommendations.isEmpty()) {
            final DocumentTemplateContext templateContext = getTemplateContext(completionTargetVariable, context);
            completionProposals = completionProposalsBuilder.computeProposals(patternRecommendations, templateContext,
                    completionTargetVariable);
        }
        return completionProposals;
    }

    /**
     * @param completionTargetVariable
     *            The variable on which the completion request was executed.
     * @param completionContext
     *            The context from where the completion request was invoked.
     * @return A {@link DocumentTemplateContext} suiting the completion context.
     */
    private DocumentTemplateContext getTemplateContext(final CompletionTargetVariable completionTargetVariable,
            final IIntelligentCompletionContext completionContext) {
        final ICompilationUnit compilationUnit = completionContext.getCompilationUnit();
        final Region region = completionTargetVariable.getDocumentRegion();
        JavaContext templateContext = null;
        try {
            templateContext = new ModifiedJavaContext(templateContextType, new Document(compilationUnit.getSource()),
                    region.getOffset(), region.getLength(), compilationUnit);
            templateContext.setForceEvaluation(true);
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e);
        }
        return templateContext;
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
    }
}
