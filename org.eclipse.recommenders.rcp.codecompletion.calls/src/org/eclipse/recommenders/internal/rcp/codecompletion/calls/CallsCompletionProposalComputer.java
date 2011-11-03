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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static java.util.Collections.emptyList;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsFalse;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.rcp.codecompletion.CompletionProposalDecorator;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("restriction")
public class CallsCompletionProposalComputer implements IJavaCompletionProposalComputer {

	private static final int MAX_NUM_PROPOSALS = 5;
	private static final double MIN_PROBABILITY_THRESHOLD = 0.1d;

	private Set<Class<?>> supportedCompletionRequests;
	private final ProjectServices projectServices;
	private final IntelligentCompletionContextResolver contextResolver;
	private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IIntelligentCompletionContext ctx;

	private Collection<CallsRecommendation> recommendations;
	private List<IJavaCompletionProposal> proposals;

	private IObjectMethodCallsNet model;

	private Variable receiver;

	private ITypeName receiverType;
	private IMethodName firstMethodDeclaration;
	private DefinitionSite.Kind receiverDefinitionKind;
	private IMethodName receiverDefinition;
	private Set<IMethodName> receiverMethodInvocations;
	private ObjectUsage query;

	@Inject
	public CallsCompletionProposalComputer(final ProjectServices projectServices,
			final Provider<Set<IVariableUsageResolver>> usageResolversProvider,
			final IntelligentCompletionContextResolver contextResolver) {
		this.projectServices = projectServices;
		this.usageResolversProvider = usageResolversProvider;
		this.contextResolver = contextResolver;
		initializeSuportedCompletionRequests();
	}

	private void initializeSuportedCompletionRequests() {
		supportedCompletionRequests = Sets.newHashSet();
		supportedCompletionRequests.add(CompletionOnMemberAccess.class);
		supportedCompletionRequests.add(CompletionOnMessageSend.class);
		supportedCompletionRequests.add(CompletionOnQualifiedNameReference.class);
		supportedCompletionRequests.add(CompletionOnSingleNameReference.class);
	}

	@Override
	public List<?> computeCompletionProposals(final ContentAssistInvocationContext context,
			final IProgressMonitor monitor) {
		final JavaContentAssistInvocationContext jCtx = (JavaContentAssistInvocationContext) context;
		if (contextResolver.hasProjectRecommendersNature(jCtx)) {
			final IIntelligentCompletionContext iCtx = contextResolver.resolveContext(jCtx);
			return computeProposals(iCtx);
		} else {
			return Collections.emptyList();
		}
	}

	private List<IJavaCompletionProposal> computeProposals(final IIntelligentCompletionContext ctx) {
		this.ctx = ctx;
		if (!canComputeProposal()) {
			return emptyList();
		}

		findRecommendations();
		findMatchingProposals();
		releaseModel();
		return this.proposals;
	}

	private boolean canComputeProposal() {
		if (!isCompletionRequestSupported()) {
			logger.info("completion request not supported");
			return false;
		}
		if (!findReceiverInContext()) {
			logger.info("receiver not found in context");
			return false;
		}
		if (!resolveObjectUsage()) {
			logger.info("object usage could not be resolved");
			return false;
		}
		if (!findModel()) {
			logger.info("no model could be found");
			return false;
		}
		return true;
	}

	private boolean isCompletionRequestSupported() {
		final ASTNode node = ctx.getCompletionNode();
		return node == null ? false : supportedCompletionRequests.contains(node.getClass());
	}

	private boolean findReceiverInContext() {
		receiver = ctx.getVariable();
		return receiver != null;
	}

	private boolean resolveObjectUsage() {
		firstMethodDeclaration = ctx.getEnclosingMethodsFirstDeclaration();
		// receiverMethodInvocations = receiver.getReceiverCalls();
		for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
			if (resolver.canResolve(ctx)) {

				logger.info("resolving with: " + resolver.getClass());

				// XXX this is a bit dirty: we need to handle this
				// appropriately... here ctx.getVariable() makes a hack for the
				// recevier type which noone can understand in 3 months.
				receiverType = receiver.isThis() ? receiver.type : ctx.getReceiverType();
				receiverMethodInvocations = resolver.getReceiverMethodInvocations();
				receiver = resolver.getResolvedVariable();

				for (IMethodName call : receiverMethodInvocations) {
					ensureIsFalse(call.isInit(), "calls contained init");
				}

				receiverDefinition = resolver.getResolvedVariableDefinition();
				receiverDefinitionKind = resolver.getResolvedVariableKind();

				ensureIsNotNull(receiverDefinition, "definition is null");

				query = createQuery();

				return true;
			}
		}
		return false;
	}

	private ObjectUsage createQuery() {
		ObjectUsage query = new ObjectUsage();
		query.type = receiverType;
		query.contextFirst = firstMethodDeclaration;
		query.kind = receiverDefinitionKind;
		query.definition = receiverDefinition;
		query.calls = receiverMethodInvocations;
		return query;
	}

	private boolean findModel() {
		final IJavaProject javaProject = ctx.getCompilationUnit().getJavaProject();
		final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);

		if (modelFacade.hasModel(receiverType)) {
			model = modelFacade.acquireModel(receiverType);
		}
		return model != null;
	}

	private void findRecommendations() {
		recommendations = Lists.newLinkedList();

		model.setQuery(query);

		final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model
				.getRecommendedMethodCalls(MIN_PROBABILITY_THRESHOLD);

		for (final Tuple<IMethodName, Double> recommended : recommendedMethodCalls) {
			final IMethodName method = recommended.getFirst();
			final Double probability = recommended.getSecond();
			if (ctx.expectsReturnValue() && method.isVoid()) {
				continue;
			}
			final CallsRecommendation recommendation = CallsRecommendation.create(receiver, method, probability);
			recommendations.add(recommendation);
		}
	}

	private void findMatchingProposals() {
		this.proposals = Lists.newLinkedList();
		for (final CompletionProposal eclProposal : ctx.getJdtProposals()) {
			switch (eclProposal.getKind()) {
			case CompletionProposal.METHOD_REF:
			case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
			case CompletionProposal.METHOD_NAME_REFERENCE:
				createCallProposalIfRecommended(eclProposal);
			}
		}

		Collections.sort(proposals, new Comparator<IJavaCompletionProposal>() {

			@Override
			public int compare(IJavaCompletionProposal o1, IJavaCompletionProposal o2) {
				return new Integer(o2.getRelevance()).compareTo(o1.getRelevance());
			}
		});

		List<IJavaCompletionProposal> intelligentProposals = Lists.newArrayList();
		for (IJavaCompletionProposal proposal : proposals) {
			if (intelligentProposals.size() < MAX_NUM_PROPOSALS) {
				intelligentProposals.add(proposal);
			}
		}
		proposals.clear();
		proposals.addAll(intelligentProposals);
		// TODO kŸrzen
	}

	private void createCallProposalIfRecommended(final CompletionProposal proposal) {

		final ProposalMatcher matcher = new ProposalMatcher(proposal);

		for (final CallsRecommendation call : recommendations) {
			if (matcher.matches(call.method)) {
				final IJavaCompletionProposal javaProposal = ctx.toJavaCompletionProposal(proposal);
				final CompletionProposalDecorator decoratedProposal = new CompletionProposalDecorator(javaProposal,
						call);

				proposals.add(decoratedProposal);
			}
		}
	}

	private void releaseModel() {
		if (model != null) {
			final IJavaProject javaProject = ctx.getCompilationUnit().getJavaProject();
			final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
			modelFacade.releaseModel(model);
			model = null;
		}
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public List<?> computeContextInformation(final ContentAssistInvocationContext context,
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
