/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.engine;

import static java.lang.Math.rint;
import static org.eclipse.recommenders.internal.completion.rcp.ProcessableCompletionProposalComputer.NULL_PROPOSAL;
import static org.eclipse.recommenders.utils.Constants.UNKNOWN_METHOD;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.SimpleProposalProcessor;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.CallPreferencePage;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionModule.CallCompletion;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.internal.utils.codestructs.Variable;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.recommenders.utils.rcp.ast.MethodDeclarationFinder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

// TODO factor the recommender interface out of the completion engine
public class CallsSessionProcessor extends SessionProcessor {

    @SuppressWarnings("serial")
    private final Set<Class<?>> supportedCompletionRequests = new HashSet<Class<?>>() {
        {
            add(CompletionOnMemberAccess.class);
            add(CompletionOnMessageSend.class);
            add(CompletionOnQualifiedNameReference.class);
            add(CompletionOnSingleNameReference.class);
        }
    };

    private final JavaElementResolver jdtResolver;

    private IRecommendersCompletionContext ctx;
    private String receiverName;
    private IType receiverType;
    @VisibleForTesting
    public ObjectUsage query;
    private IObjectMethodCallsNet model;
    private List<CallsRecommendation> recommendations;

    private final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore;

    private final IPreferenceStore prefStore;

    @Inject
    public CallsSessionProcessor(final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore,
            final JavaElementResolver jdtResolver, @CallCompletion final IPreferenceStore prefStore) {
        this.modelStore = modelStore;
        this.jdtResolver = jdtResolver;
        this.prefStore = prefStore;
    }

    @Override
    public void startSession(IRecommendersCompletionContext context) {
        ctx = context;
        query = ObjectUsage.newObjectUsageWithDefaults();
        recommendations = Lists.newLinkedList();

        try {
            if (!isCompletionRequestSupported() || //
                    !findReceiver() || //
                    !acquireModel() || //
                    !completeQuery() || //
                    !findRecommendations()) {
                return;
            }
        } finally {
            releaseModel();
        }
    }

    private boolean isCompletionRequestSupported() {
        final ASTNode node = ctx.getCompletionNode().orNull();
        return node == null ? false : supportedCompletionRequests.contains(node.getClass());
    }

    private boolean findReceiver() {
        receiverName = ctx.getReceiverName();
        receiverType = ctx.getReceiverType().orNull();
        if (isReceiverNameThis() || isReceiverNameSuper() || isImplicitThis()) {
            receiverName = "";
            // receiver may be this!
            setReceiverToSupertype();
        }
        return receiverType != null;
    }

    private boolean isReceiverNameThis() {
        return "this".equals(receiverName);
    }

    private boolean isReceiverNameSuper() {
        return "super".equals(receiverName);
    }

    private boolean isImplicitThis() {
        return receiverType == null;// && receiverName.isEmpty();
    }

    private void setReceiverToSupertype() {
        try {
            final IMethod m = ctx.getEnclosingMethod().orNull();
            if (m == null || JdtFlags.isStatic(m)) {
                return;
            }
            final IType type = m.getDeclaringType();
            final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
            receiverType = hierarchy.getSuperclass(type);
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "Failed to resolve super type of %s", ctx.getEnclosingElement());
        }
    }

    private boolean acquireModel() {
        model = modelStore.aquireModel(receiverType).orNull();
        return model != null;
    }

    private boolean completeQuery() {
        setCalls();
        setReceiverType();
        setFirstMethodDeclaration();
        setDefinition();
        return true;
    }

    private void setDefinition() {
        if (query.definition.equals(UNKNOWN_METHOD)) {
            final Optional<IMethodName> methodDef = ctx.getMethodDef();
            if (methodDef.isPresent()) {
                query.definition = methodDef.get();
                query.kind = Kind.METHOD_RETURN;
            } else if (query.kind == Kind.UNKNOWN) {
                query.kind = Kind.FIELD;
            }
        } else if (query.definition != null && query.kind == Kind.PARAMETER) {
            query.definition = query.contextFirst;
        }
    }

    private void setFirstMethodDeclaration() {
        final Optional<IMethod> enclosingMethod = ctx.getEnclosingMethod();
        if (!enclosingMethod.isPresent()) {
            return;
        }
        final IMethod first = JdtUtils.findFirstDeclaration(enclosingMethod.get());
        query.contextFirst = jdtResolver.toRecMethod(first).or(VmMethodName.NULL);
    }

    private void setReceiverType() {
        query.type = jdtResolver.toRecType(receiverType);
    }

    private void setCalls() {
        final CompilationUnit ast = ctx.getAST();
        final Optional<IMethod> enclosingMethod = ctx.getEnclosingMethod();
        if (enclosingMethod.isPresent()) {
            final IMethod jdtMethod = enclosingMethod.get();
            final IMethodName recMethod = jdtResolver.toRecMethod(jdtMethod).or(VmMethodName.NULL);
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
                    final Optional<IMethodName> def = ctx.getMethodDef();
                    if (def.isPresent()) {
                        query.definition = def.get();
                    }
                }
            }
        }
    }

    private boolean findRecommendations() {

        model.setQuery(query);

        final double minProbability = prefStore.getInt(CallPreferencePage.ID_MIN_PROBABILITY) * 0.01;
        final int maxProposals = prefStore.getInt(CallPreferencePage.ID_MAX_PROPOSALS);
        final SortedSet<Pair<IMethodName, Double>> recommendedMethodCalls =
                model.getRecommendedMethodCalls(minProbability);

        final Variable var = Variable.create(receiverName, jdtResolver.toRecType(receiverType), null);

        final boolean expectsReturnType = ctx.getExpectedTypeSignature().isPresent();
        for (final Pair<IMethodName, Double> recommended : recommendedMethodCalls) {
            final IMethodName method = recommended.getFirst();
            final Double probability = recommended.getSecond();

            if (expectsReturnType) {
                if (method.isVoid()) {
                    continue;
                }
            }
            final CallsRecommendation recommendation = CallsRecommendation.create(var, method, probability);
            recommendations.add(recommendation);

        }
        recommendations = recommendations.subList(0, Math.min(recommendations.size(), maxProposals));
        return !recommendations.isEmpty();
    }

    private void releaseModel() {
        if (model != null) {
            modelStore.releaseModel(model);
            model = null;
        }
    }

    @Override
    public void process(IProcessableProposal proposal) {
        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.METHOD_NAME_REFERENCE:
            final ProposalMatcher matcher = new ProposalMatcher(coreProposal);
            for (final CallsRecommendation call : recommendations) {
                final IMethodName crMethod = call.method;
                if (!matcher.match(crMethod)) continue;

                final int percentage = (int) rint(call.probability * 100);
                int increment = 200 + percentage;
                String label = percentage + " %";
                proposal.getProposalProcessorManager().addProcessor(new SimpleProposalProcessor(increment, label));
            }
        }
    }
}
