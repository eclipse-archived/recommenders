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
package org.eclipse.recommenders.internal.completion.rcp.calls.engine;

import static java.util.Collections.emptyList;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.CompletionProposalDecorator;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.recommenders.utils.rcp.ast.MethodDeclarationFinder;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class CallsCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private static final int BASIS_RELEVANCE = 1600;

    // private static final int MAX_NUM_PROPOSALS = 5;
    private static final double MIN_PROBABILITY_THRESHOLD = 0.1d;

    @SuppressWarnings("serial")
    private final Set<Class<?>> supportedCompletionRequests = new HashSet<Class<?>>() {
        {
            add(CompletionOnMemberAccess.class);
            add(CompletionOnMessageSend.class);
            add(CompletionOnQualifiedNameReference.class);
            add(CompletionOnSingleNameReference.class);
        }
    };

    private final CallModelStore modelStore;
    private final IRecommendersCompletionContextFactory ctxFactory;
    private final JavaElementResolver jdtResolver;

    private IRecommendersCompletionContext ctx;
    private String receiverName;
    private IType receiverType;
    private ObjectUsage query;
    private IObjectMethodCallsNet model;
    private List<ICompletionProposal> proposals;
    private LinkedList<CallsRecommendation> recommendations;

    private JavaContentAssistInvocationContext javaContext;

    @Inject
    public CallsCompletionProposalComputer(final CallModelStore modelStore, final JavaElementResolver jdtResolver,
            final IRecommendersCompletionContextFactory ctxFactory) {
        this.modelStore = modelStore;
        this.jdtResolver = jdtResolver;
        this.ctxFactory = ctxFactory;
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext javaContext,
            final IProgressMonitor monitor) {
        initalize(javaContext);

        if (!isCompletionRequestSupported()) {
            return emptyList();
        }
        if (!findReceiver()) {
            return emptyList();
        }
        if (!acquireModel()) {
            return emptyList();
        }
        if (!completeQuery()) {
            return emptyList();
        }
        findRecommendations();
        createProspsals();
        releaseModel();

        return proposals;

    }

    private void initalize(final ContentAssistInvocationContext javaContext) {
        this.javaContext = (JavaContentAssistInvocationContext) javaContext;
        ctx = ctxFactory.create(this.javaContext);
        query = ObjectUsage.newObjectUsageWithDefaults();
        proposals = Lists.newLinkedList();
    }

    private boolean isCompletionRequestSupported() {
        final ASTNode node = ctx.getCompletionNode();
        return node == null ? false : supportedCompletionRequests.contains(node.getClass());
    }

    private boolean findReceiver() {
        receiverName = ctx.getReceiverName();
        receiverType = ctx.getReceiverType().orNull();
        if (receiverType == null && receiverName.isEmpty()) {
            // receiver may be this!
        }
        return receiverType != null;
    }

    private boolean acquireModel() {
        model = modelStore.aquireModel(receiverType).orNull();
        return model != null;
    }

    private boolean completeQuery() {
        setCalls();
        setReceiverType();
        setFirstMethodDeclaration();
        return true;
    }

    private void setFirstMethodDeclaration() {
        final Optional<IMethod> enclosingMethod = ctx.getEnclosingMethod();
        if (!enclosingMethod.isPresent()) {
            return;
        }
        final IMethod first = JdtUtils.findFirstDeclaration(enclosingMethod.get());
        query.contextFirst = jdtResolver.toRecMethod(first);
    }

    private void setReceiverType() {
        query.type = jdtResolver.toRecType(receiverType);
    }

    private void setCalls() {
        final CompilationUnit ast = ctx.getAST();
        final Optional<IMethod> enclosingMethod = ctx.getEnclosingMethod();
        if (enclosingMethod.isPresent()) {
            final IMethod jdtMethod = enclosingMethod.get();
            final IMethodName recMethod = jdtResolver.toRecMethod(jdtMethod);
            final Optional<MethodDeclaration> astMethod = MethodDeclarationFinder.find(ast, recMethod);
            if (astMethod.isPresent()) {
                final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
                final ObjectUsage usage = r.findObjectUsage(receiverName, astMethod.get());
                query.calls = usage.calls;
                if (usage.kind != null) {
                    query.kind = usage.kind;
                }
                if (usage.definition != null) {
                    query.definition = usage.definition;
                }
            }
        }
    }

    private void findRecommendations() {
        recommendations = Lists.newLinkedList();

        model.setQuery(query);

        final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model
                .getRecommendedMethodCalls(MIN_PROBABILITY_THRESHOLD);

        final Variable var = Variable.create(receiverName, jdtResolver.toRecType(receiverType), null);

        boolean expectsReturnType = ctx.getExpectedTypeSignature().isPresent();
        boolean expectsNonPrimitiveReturnType = ctx.getExpectedType().isPresent();
        boolean expectsPrimitive = !expectsNonPrimitiveReturnType;

        for (final Tuple<IMethodName, Double> recommended : recommendedMethodCalls) {

            final IMethodName method = recommended.getFirst();
            final Double probability = recommended.getSecond();

            if (expectsReturnType) {
                ITypeName returnType = method.getReturnType();
                if (method.isVoid()) {
                    continue;
                }
                if (expectsPrimitive && !samePrimitiveType(returnType, ctx.getExpectedTypeSignature().or(""))) {
                    continue;
                }
            }
            final CallsRecommendation recommendation = CallsRecommendation.create(var, method, probability);
            recommendations.add(recommendation);
        }
    }

    private boolean samePrimitiveType(final ITypeName returnType, final String lhs) {
        String rhs = returnType.getIdentifier();
        return rhs.equals(lhs);
    }

    private void createProspsals() {
        final String prefix = ctx.getPrefix();
        for (final CallsRecommendation r : recommendations) {
            if (!r.method.getName().startsWith(prefix)) {
                continue;
            }

            final IMethod method = jdtResolver.toJdtMethod(r.method);
            if (method == null) {
                continue;
            }

            final int start = ctx.getInvocationOffset() - ctx.getPrefix().length();
            final int end = ctx.getInvocationOffset();
            final CompletionProposal proposal = JdtUtils.createProposal(method, CompletionProposal.METHOD_REF, start,
                    end, end);
            int proposalPercentage = (int) Math.rint(r.probability * 100);
            int rating = BASIS_RELEVANCE + proposalPercentage;
            proposal.setRelevance(rating);

            final ParameterGuessingProposal javaProposal = ParameterGuessingProposal.createProposal(proposal,
                    ctx.getJavaContext(), true);
            final CompletionProposalDecorator decorator = new CompletionProposalDecorator(javaProposal, r.probability);
            proposals.add(decorator);
        }
        if (!proposals.isEmpty()) {
            proposals.add(new SeparatorProposal(ctx.getInvocationOffset()));
        }
    }

    private void releaseModel() {
        if (model != null) {
            modelStore.releaseModel(model);
            model = null;
        }
    }

    @Override
    public void sessionStarted() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
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

    private final class SeparatorProposal extends JavaCompletionProposal {
        private SeparatorProposal(final int replacementOffset) {
            super("", replacementOffset, 0, null, "--------------", BASIS_RELEVANCE);
        }

        @Override
        protected boolean isPrefix(final String prefix, final String string) {
            // remove this proposal as soon as none of our proposal can be applied anymore:
            for (ICompletionProposal p : proposals) {
                // skip me myself
                if (p == this) {
                    continue;
                }
                if (p instanceof ICompletionProposalExtension2) {
                    ICompletionProposalExtension2 p2 = (ICompletionProposalExtension2) p;
                    IDocument doc = javaContext.getDocument();
                    int newOffset = javaContext.getInvocationOffset() + prefix.length();
                    if (p2.validate(doc, newOffset, null)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
