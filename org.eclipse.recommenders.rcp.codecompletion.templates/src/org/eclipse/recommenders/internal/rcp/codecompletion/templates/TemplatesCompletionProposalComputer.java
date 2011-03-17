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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnLocalName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
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
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
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
public final class TemplatesCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private final IntelligentCompletionContextResolver contextResolver;
    private final PatternRecommender patternRecommender;
    private CompletionProposalsBuilder completionProposalsBuilder;
    private AbstractJavaContextType templateContextType;

    /**
     * @param patternRecommender
     *            Computes and returns patterns suitable for the active
     *            completion request.
     * @param codeBuilder
     *            The {@link CodeBuilder} will turn {@link MethodCall}s into the
     *            code to be used by the eclipse template engine.
     * @param contextResolver
     *            Responsible for computing an
     *            {@link IIntelligentCompletionContext} from a
     *            {@link ContentAssistInvocationContext} which is given by the
     *            editor for each completion request.
     */
    @Inject
    public TemplatesCompletionProposalComputer(final PatternRecommender patternRecommender,
            final CodeBuilder codeBuilder, final IntelligentCompletionContextResolver contextResolver) {
        this.patternRecommender = patternRecommender;
        this.contextResolver = contextResolver;
        initializeProposalBuilder(codeBuilder);
        initializeTemplateContextType();
    }

    /**
     * Initializes the proposal builder.
     * 
     * @param codeBuilder
     *            The {@link CodeBuilder} will turn {@link MethodCall}s into the
     *            code to be used by the eclipse template engine.
     */
    private void initializeProposalBuilder(final CodeBuilder codeBuilder) {
        final Bundle bundle = FrameworkUtil.getBundle(TemplatesCompletionProposalComputer.class);
        Image icon = null;
        if (bundle != null) {
            icon = AbstractUIPlugin.imageDescriptorFromPlugin(bundle.getSymbolicName(), "metadata/icon2.gif")
                    .createImage();
        }
        completionProposalsBuilder = new CompletionProposalsBuilder(icon, codeBuilder);
    }

    /**
     * Sets the appropriate <code>ContextType</code> for all computed templates.
     */
    private void initializeTemplateContextType() {
        final JavaPlugin plugin = JavaPlugin.getDefault();
        if (plugin != null) {
            templateContextType = (AbstractJavaContextType) plugin.getTemplateContextRegistry().getContextType(
                    JavaContextType.ID_ALL);
        }
    }

    @Override
    public List<IJavaCompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
        if (contextResolver.hasProjectRecommendersNature(jCtx)) {
            return computeCompletionProposals(contextResolver.resolveContext(jCtx));
        }
        return Collections.emptyList();
    }

    /**
     * @param context
     *            The context from where the completion request was invoked.
     * @return The completion proposals to be displayed in the editor.
     */
    public ImmutableList<IJavaCompletionProposal> computeCompletionProposals(final IIntelligentCompletionContext context) {
        if (shouldComputeProposalsForContext(context)) {
            final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
                    .createInvokedVariable(context);
            if (completionTargetVariable != null) {
                return buildProposalsForTargetVariable(completionTargetVariable, context);
            }
        }
        return ImmutableList.of();
    }

    /**
     * @param context
     *            The context from where the completion request was invoked.
     * @return True, if the computer should try to find proposals for the given
     *         context.
     */
    private boolean shouldComputeProposalsForContext(final IIntelligentCompletionContext context) {
        if (context.getEnclosingMethod() == null) {
            return false;
        }
        if (!context.expectsReturnValue()) {
            return !(context.getCompletionNode() instanceof CompletionOnMemberAccess);
        }
        return context.getCompletionNode() instanceof CompletionOnLocalName
                || context.getCompletionNode() instanceof CompletionOnSingleNameReference;
    }

    /**
     * @param completionTargetVariable
     *            The variable on which the completion request was executed.
     * @param context
     *            The context from where the completion request was invoked.
     * @return The completion proposals to be displayed in the editor.
     */
    private ImmutableList<IJavaCompletionProposal> buildProposalsForTargetVariable(
            final CompletionTargetVariable completionTargetVariable, final IIntelligentCompletionContext context) {
        final Collection<PatternRecommendation> patternRecommendations = patternRecommender.computeRecommendations(
                completionTargetVariable, context);
        if (!patternRecommendations.isEmpty()) {
            final DocumentTemplateContext templateContext = getTemplateContext(completionTargetVariable, context);
            return completionProposalsBuilder.computeProposals(patternRecommendations, templateContext,
                    completionTargetVariable.getName());
        }
        return ImmutableList.of();
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
            // TODO: new ModifiedJavaContext
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
