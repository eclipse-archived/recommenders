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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;

/**
 * Computes context-sensitive {@link PatternRecommendation}s from the
 * {@link CallsModelStore}.
 */
public final class PatternRecommender {

    private static final int MAX_PATTERNS = 5;
    private static final double PATTERN_PROBABILITY_THRESHOLD = 0.02d;
    private static final double METHOD_PROBABILITY_THRESHOLD = 0.1d;

    private final CallsModelStore callsModelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolvers;

    private IIntelligentCompletionContext context;
    private Set<IMethodName> receiverMethodInvocations;
    private IObjectMethodCallsNet model;

    /**
     * @param callsModelStore
     *            The place where all patterns for all available classes are
     *            stored.
     * @param usageResolvers
     *            A set of resolvers which are able to compute a variable's type
     *            and preceding method invocations from a given context.
     */
    @Inject
    public PatternRecommender(final CallsModelStore callsModelStore,
            final Provider<Set<IVariableUsageResolver>> usageResolvers) {
        this.callsModelStore = callsModelStore;
        this.usageResolvers = usageResolvers;
    }

    /**
     * @param targetVariable
     *            The variable information which could be extracted from the
     *            completion context.
     * @return The {@link PatternRecommendation}s holding information for the
     *         templates to be displayed.
     */
    public ImmutableSet<PatternRecommendation> computeRecommendations(final CompletionTargetVariable targetVariable) {
        final Builder<PatternRecommendation> recommendations = ImmutableSet.builder();
        context = targetVariable.getContext();
        if (canFindVariableUsage(targetVariable)) {
            for (final IObjectMethodCallsNet typeModel : findModelsForType(targetVariable.getType())) {
                model = typeModel;
                updateModel();
                recommendations.addAll(computeRecommendationsForModel(targetVariable.isNeedsConstructor()));
            }
        }
        return recommendations.build();
    }

    /**
     * @param targetVariable
     *            The variable on which the completion request was invoked.
     * @return True, if a new variable is to be constructed or an existing's
     *         usage could be resolved.
     */
    private boolean canFindVariableUsage(final CompletionTargetVariable targetVariable) {
        boolean result = true;
        if (context.getVariable() == null) {
            receiverMethodInvocations = targetVariable.getReceiverCalls();
        } else {
            result = canResolveVariableUsage();
        }
        return result;
    }

    /**
     * @return True, if a provided {@link IVariableUsageResolver} was able to
     *         resolve the variable usage.
     */
    private boolean canResolveVariableUsage() {
        for (final IVariableUsageResolver resolver : usageResolvers.get()) {
            if (resolver.canResolve(context)) {
                receiverMethodInvocations = resolver.getReceiverMethodInvocations();
                return true;
            }
        }
        return false;
    }

    /**
     * @param receiverType
     *            The type for which suiteable models should be found. Can be
     *            fully qualified and simple.
     * @return Empty set when no model could be found. One-element set when the
     *         type is fully qualified. Multiple elements when the type is
     *         simple (i.e. it matches several classes).
     */
    private ImmutableSet<IObjectMethodCallsNet> findModelsForType(final ITypeName receiverType) {
        final Builder<IObjectMethodCallsNet> models = ImmutableSet.builder();
        if (receiverType.getPackage().isDefaultPackage()) {
            models.addAll(callsModelStore.getModelsForSimpleName(receiverType));
        } else if (callsModelStore.hasModel(receiverType)) {
            models.add(callsModelStore.getModel(receiverType));
        }
        return models.build();
    }

    /**
     * Updates the model with respect to the context and the observed method
     * invocations on the target variable.
     */
    private void updateModel() {
        model.clearEvidence();
        model.setMethodContext(context.getEnclosingMethodsFirstDeclaration());
        model.setObservedMethodCalls(model.getType(), receiverMethodInvocations);
        if (shallNegateConstructors(context.getVariable())) {
            model.negateConstructors();
        }
        model.updateBeliefs();
    }

    /**
     * @param contextVariable
     *            The target variable as given by the context.
     * @return True, if the patterns should definitely contain no constructors.
     */
    private static boolean shallNegateConstructors(final Variable contextVariable) {
        return contextVariable != null
                && (contextVariable.fuzzyIsParameter() || contextVariable.fuzzyIsDefinedByMethodReturn());
    }

    /**
     * @param constructorRequired
     *            True, if patterns without constructors should be filtered out.
     * @return The most probable patterns regarding the updated model, limited
     *         to the size of <code>MAX_PATTERNS</code>.
     */
    private ImmutableSet<PatternRecommendation> computeRecommendationsForModel(final boolean constructorRequired) {
        final Set<PatternRecommendation> typeRecs = Sets.newTreeSet();
        for (final Tuple<String, Double> patternWithProbablity : findMostLikelyPatterns()) {
            final String patternName = patternWithProbablity.getFirst();
            final List<IMethodName> patternMethods = getMethodCallsForPattern(patternName);
            if (shouldKeepPattern(patternMethods, constructorRequired)) {
                final int percentage = (int) (patternWithProbablity.getSecond().doubleValue() * 100);
                typeRecs.add(new PatternRecommendation(patternName, model.getType(), patternMethods, percentage));
            }
        }
        return ImmutableSet.copyOf(typeRecs);
    }

    /**
     * @return Most probable pattern names and their probabilities, trimmed to
     *         the size of <code>MAX_PATTERNS</code>.
     */
    private ImmutableList<Tuple<String, Double>> findMostLikelyPatterns() {
        List<Tuple<String, Double>> patterns = model.getPatternsWithProbability();
        patterns = Lists.newArrayList(Iterators.filter(patterns.iterator(), new PatternProbabilityFilter()));
        Collections.sort(patterns, new PatternSorter());
        patterns = patterns.subList(0, Math.min(patterns.size(), MAX_PATTERNS));
        return ImmutableList.copyOf(patterns);
    }

    /**
     * @param patternName
     *            The internal pattern name, used to identify the pattern inside
     *            the pattern store.
     * @return The methods which shall be invoked by the template built from the
     *         given pattern.
     */
    private ImmutableList<IMethodName> getMethodCallsForPattern(final String patternName) {
        final com.google.common.collect.ImmutableList.Builder<IMethodName> recommendedMethods = ImmutableList.builder();
        model.setPattern(patternName);
        model.updateBeliefs();
        for (final Tuple<IMethodName, Double> pair : model.getRecommendedMethodCalls(METHOD_PROBABILITY_THRESHOLD)) {
            recommendedMethods.add(pair.getFirst());
        }
        return recommendedMethods.build();
    }

    /**
     * @param patternMethods
     *            The methods of a given pattern as received from the mode
     *            store.
     * @param constructorRequired
     *            True, if patterns without constructors should be filtered out.
     * @return True, if methods exist and they either contain a constructor or a
     *         constructor is not required.
     */
    private boolean shouldKeepPattern(final List<IMethodName> patternMethods, final boolean constructorRequired) {
        if (patternMethods.isEmpty() || constructorRequired && !patternMethods.get(0).isInit()) {
            return false;
        }
        if (constructorRequired || context.getReceiverType() == null) {
            return true;
        }
        final String prefixToken = context.getPrefixToken();
        Preconditions.checkNotNull(prefixToken);
        for (final IMethodName method : patternMethods) {
            if (StringUtils.startsWithIgnoreCase(method.getName(), prefixToken)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A {@link Predicate} which filters out patterns not surpassing a minimum
     * probability threshold.
     */
    static final class PatternProbabilityFilter implements Predicate<Tuple<String, Double>> {
        @Override
        public boolean apply(final Tuple<String, Double> pattern) {
            return pattern.getSecond().doubleValue() > PATTERN_PROBABILITY_THRESHOLD;
        }
    }

    /**
     * A {@link Comparator} sorting patterns in their probabilities descending
     * order or by their name in case of same probabilities.
     */
    static final class PatternSorter implements Comparator<Tuple<String, Double>> {
        @Override
        public int compare(final Tuple<String, Double> pattern1, final Tuple<String, Double> pattern2) {
            int probabilityOrder = pattern2.getSecond().compareTo(pattern1.getSecond());
            if (probabilityOrder == 0) {
                probabilityOrder = pattern1.getFirst().compareTo(pattern2.getFirst());
            }
            return probabilityOrder;
        }
    }
}
