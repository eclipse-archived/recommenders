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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalLabelProvider;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
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

    private final IRecommendersCompletionContextFactory ctxFactory;
    private final InstantOverridesRecommender recommender;
    private final JavaElementResolver jdtCache;

    private IRecommendersCompletionContext ctx;
    private IType enclosingType;
    private IType supertype;
    private List<OverridesRecommendation> recommendations;
    private List<IJavaCompletionProposal> proposals;

    @Inject
    public OverridesCompletionProposalComputer(final InstantOverridesRecommender recommender,
            final IRecommendersCompletionContextFactory ctxFactory, final JavaElementResolver jdtCache) {
        this.recommender = recommender;
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
        return recommender.isSuperclassSupported();
    }

    private void computeRecommendations() throws JavaModelException {
        final TypeDeclaration query = computeQuery();
        recommendations = recommender.createRecommendations(query);
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
        for (final OverridesRecommendation r : recommendations) {
            if (!r.method.getName().startsWith(prefix)) {
                continue;
            }

            final Optional<IMethod> optMethod = jdtCache.toJdtMethod(r.method);
            if (!optMethod.isPresent()) {
                continue;
            }
            final IMethod method = optMethod.get();

            final int start = ctx.getInvocationOffset() - prefix.length();
            final int end = ctx.getInvocationOffset();
            final CompletionProposal proposal = JdtUtils.createProposal(method, CompletionProposal.METHOD_DECLARATION,
                    start, end, end);

            final StringBuffer sb = new StringBuffer();
            try {
                sb.append(Flags.toString(method.getFlags()));
                sb.append(" ");
                sb.append(Signature.getSignatureSimpleName(method.getReturnType()));
                sb.append(" ");
                sb.append(method.getElementName());
                sb.append("(");
                final String[] parameterTypes = method.getParameterTypes();
                for (final String param : parameterTypes) {
                    final String name = Signature.getSignatureSimpleName(param);
                    sb.append(name);
                    sb.append(" %, ");
                }
                if (parameterTypes.length > 0) {
                    sb.delete(sb.length() - 2, sb.length());
                }
                sb.append(")");
            } catch (final JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            proposal.setCompletion(sb.toString().toCharArray());
            proposal.setRelevance(100 + (int) Math.rint(r.probability * 100));

            final CompletionProposalLabelProvider fLabelProvider = new CompletionProposalLabelProvider();
            final StyledString createStyledLabel = fLabelProvider.createStyledLabel(proposal);
            final String[] parameterTypes = method.getParameterTypes();
            final String[] proposalParameterTypes = new String[parameterTypes.length];
            try {
                for (int i = 0; i < parameterTypes.length; i++) {
                    proposalParameterTypes[i] = Signature.toString(parameterTypes[i]);
                }
            } catch (final IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final JavaCompletionProposal javaProposal = new OverrideCompletionProposal(ctx.getProject(),
                    ctx.getCompilationUnit(), method.getElementName(), proposalParameterTypes, start, ctx.getPrefix()
                            .length(), createStyledLabel, String.valueOf(proposal.getCompletion()));
            javaProposal.setImage(fLabelProvider.createImageDescriptor(proposal).createImage());
            javaProposal.setProposalInfo(new MethodProposalInfo(ctx.getProject(), proposal));
            javaProposal.setRelevance(proposal.getRelevance() << 20);

            final CompletionProposalDecorator decorator = new CompletionProposalDecorator(javaProposal, r.probability);
            proposals.add(decorator);
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
