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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.CompletionProposalDecorator;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.completion.rcp.CompilerBindings;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class OverridesCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private final InstantOverridesRecommender recommender;
    private final IntelligentCompletionContextResolver contextResolver;
    private final JavaElementResolver jdtCache;

    private JavaContentAssistInvocationContext jdtContext;
    private IIntelligentCompletionContext recContext;
    private List<OverridesRecommendation> recommendations;
    private List<IJavaCompletionProposal> proposals;

    private IType enclosingType;
    private IType supertype;
    private List<IMethodName> declaredMethods;

    @Inject
    public OverridesCompletionProposalComputer(final InstantOverridesRecommender recommender,
            final IntelligentCompletionContextResolver contextResolver, final JavaElementResolver jdtCache) {
        this.recommender = recommender;
        this.contextResolver = contextResolver;
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
        findAllDeclaredMethods();
        computeRecommendations();
        computeProposals();
        return proposals;
    }

    private void initializeContexts(final ContentAssistInvocationContext context) {
        jdtContext = (JavaContentAssistInvocationContext) context;
        recContext = contextResolver.resolveContext(jdtContext);
    }

    private boolean findEnclosingType() {
        final IJavaElement enclosingElement = jdtContext.getCoreContext().getEnclosingElement();
        if (enclosingElement instanceof IType) {
            enclosingType = (IType) enclosingElement;
        }
        return enclosingType != null;
    }

    private boolean findSuperclass() {
        supertype = JdtUtils.findSuperclass(enclosingType).orNull();
        return supertype != null;
    }

    /**
     * Note, we just take every method we can find. there is no need to check whether it actually overrides a method in
     * a supertype. If not, the compiler would complain.
     */
    private void findAllDeclaredMethods() {
        declaredMethods = Lists.newLinkedList();
        for (final MethodDeclaration methodDeclaration : recContext.getMethodDeclarations()) {
            final Optional<IMethodName> methodName = CompilerBindings.toMethodName(methodDeclaration.binding);
            if (methodName.isPresent()) {
                declaredMethods.add(methodName.get());
            }
        }
    }

    private void computeRecommendations() {
        final TypeDeclaration query = TypeDeclaration.create(null, jdtCache.toRecType(supertype));
        for (final IMethodName name : declaredMethods) {
            query.methods.add(org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration.create(name));
        }
        recommendations = recommender.createRecommendations(query);
    }

    private void computeProposals() {
        proposals = Lists.newLinkedList();
        createFilteredOverridesRecommendations();
    }

    private void createFilteredOverridesRecommendations() {
        for (final CompletionProposal eclProposal : recContext.getJdtProposals()) {
            switch (eclProposal.getKind()) {
            case CompletionProposal.METHOD_DECLARATION:
                createOverrideProposalIfRecommended(eclProposal);
            }
        }
    }

    private void createOverrideProposalIfRecommended(final CompletionProposal eclProposal) {
        final VmMethodName ref = getProposalKeyAsVmMethodName(eclProposal);
        for (final OverridesRecommendation recommendation : recommendations) {
            if (ref.getSignature().equals(recommendation.method.getSignature())) {
                final IJavaCompletionProposal javaProposal = recContext.toJavaCompletionProposal(eclProposal);
                final CompletionProposalDecorator decoratedProposal = new CompletionProposalDecorator(javaProposal,
                        recommendation);
                proposals.add(decoratedProposal);
            }
        }
    }

    private VmMethodName getProposalKeyAsVmMethodName(final CompletionProposal eclProposal) {
        String key = String.valueOf(eclProposal.getKey()).replaceAll(";\\.", ".");
        final int exceptionSeparator = key.indexOf("|");
        if (exceptionSeparator != -1) {
            key = key.substring(0, exceptionSeparator);
        }
        return VmMethodName.get(key);
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
