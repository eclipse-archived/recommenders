/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import static java.lang.Math.rint;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.MethodDeclaration;
import org.eclipse.recommenders.internal.utils.codestructs.TypeDeclaration;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.rcp.CompletionProposalDecorator;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class OverridesCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private final double MIN_PROBABILITY_THRESHOLD = 0.1d;

    private final IRecommendersCompletionContextFactory ctxFactory;
    private IModelArchiveStore<IType, ClassOverridesNetwork> modelStore;
    private final JavaElementResolver jdtCache;

    private IRecommendersCompletionContext ctx;
    private IType enclosingType;
    private IType supertype;
    private List<OverridesRecommendation> recommendations;
    private List<IJavaCompletionProposal> proposals;
    private ClassOverridesNetwork model;

    @Inject
    public OverridesCompletionProposalComputer(IModelArchiveStore<IType, ClassOverridesNetwork> modelStore,
            final IRecommendersCompletionContextFactory ctxFactory, final JavaElementResolver jdtCache) {
        this.modelStore = modelStore;
        this.ctxFactory = ctxFactory;
        this.jdtCache = jdtCache;
    };

    @Override
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        initializeContexts(context);

        if (!findEnclosingType()) {
            return Collections.emptyList();
        }
        if (!findSuperclass()) {
            return Collections.emptyList();
        }
        if (!hasModel()) {
            return Collections.emptyList();
        }
        try {
            computeRecommendations();
            computeProposals();
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "Exception caught in overrides completion proposal computer.");
            return Collections.emptyList();
        }
        return proposals;
    }

    private void initializeContexts(final ContentAssistInvocationContext context) {
        ctx = ctxFactory.create((JavaContentAssistInvocationContext) context);
    }

    private boolean findEnclosingType() {
        enclosingType = ctx.getEnclosingType().orNull();
        return enclosingType != null;
    }

    private boolean findSuperclass() {
        supertype = JdtUtils.findSuperclass(enclosingType).orNull();
        return supertype != null;
    }

    private boolean hasModel() {
        model = modelStore.aquireModel(supertype).orNull();
        return model != null;
    }

    private void computeRecommendations() throws JavaModelException {
        final TypeDeclaration query = computeQuery();
        for (final MethodDeclaration method : query.methods) {
            model.observeMethodNode(method.superDeclaration);
        }
        recommendations = readRecommendations();
    }

    private List<OverridesRecommendation> readRecommendations() {
        final List<OverridesRecommendation> res = Lists.newLinkedList();
        for (final Tuple<IMethodName, Double> item : model.getRecommendedMethodOverrides(MIN_PROBABILITY_THRESHOLD, 5)) {
            final IMethodName method = item.getFirst();
            final Double probability = item.getSecond();
            final OverridesRecommendation recommendation = new OverridesRecommendation(method, probability);
            res.add(recommendation);
        }
        return res;
    }

    private TypeDeclaration computeQuery() throws JavaModelException {
        final TypeDeclaration query = TypeDeclaration.create(null, jdtCache.toRecType(supertype));
        for (final IMethod m : enclosingType.getMethods()) {
            final Optional<IMethod> superMethod = JdtUtils.findOverriddenMethod(m);
            if (superMethod.isPresent()) {
                final IMethodName recMethod = jdtCache.toRecMethod(m).or(VmMethodName.NULL);
                final IMethodName recSuperMethod = jdtCache.toRecMethod(superMethod.get()).or(VmMethodName.NULL);
                final MethodDeclaration create = MethodDeclaration.create(recMethod);
                create.superDeclaration = recSuperMethod;
                query.methods.add(create);
            }
        }
        return query;
    }

    private void computeProposals() {
        proposals = Lists.newLinkedList();
        final String prefix = ctx.getPrefix();
        for (Entry<IJavaCompletionProposal, CompletionProposal> pair : ctx.getProposals().entrySet()) {
            IJavaCompletionProposal uiProposal = pair.getKey();
            CompletionProposal cProposal = pair.getValue();
            switch (cProposal.getKind()) {
            case CompletionProposal.METHOD_DECLARATION:
                final String signature = String.valueOf(cProposal.getSignature()).replace('.', '/');
                final String name = String.valueOf(cProposal.getName());
                final String propSignature = (name + signature).replaceAll("<\\.>", "");
                for (final OverridesRecommendation r : recommendations) {
                    if (!r.method.getName().startsWith(prefix)) {
                        continue;
                    }

                    final String recSignature = r.method.getSignature();
                    if (recSignature.equals(propSignature)) {
                        if (uiProposal instanceof AbstractJavaCompletionProposal) {
                            int baseRelevance = uiProposal.getRelevance();
                            baseRelevance += (int) rint(r.probability * 100);
                            ((AbstractJavaCompletionProposal) uiProposal).setRelevance(baseRelevance);
                        }
                        final CompletionProposalDecorator decoratedProposal = new CompletionProposalDecorator(
                                uiProposal, r.probability);
                        proposals.add(decoratedProposal);
                    }
                }
            }
        }
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
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
