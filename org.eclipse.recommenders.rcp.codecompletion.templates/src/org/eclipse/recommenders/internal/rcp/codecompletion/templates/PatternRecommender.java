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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.ObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;

/**
 * Computes <code>PatternRecommendations</code>s from the
 * <code>CallsModelStore</code>.
 */
public final class PatternRecommender {

    private static final int MAX_PATTERNS = 5;
    private static final double PATTERN_PROBABILITY_THRESHOLD = 0.02d;
    private static final double METHOD_PROBABILITY_THRESHOLD = 0.1d;

    private final CallsModelStore callsModelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolvers;
    private ITypeName receiverType;
    private Set<IMethodName> receiverMethodInvocations;
    private ObjectMethodCallsNet model;

    /**
     * @param callsModelStore
     *            The place where all patterns for every available class are
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
     *            The variable on which the completion request was invoked.
     * @param context
     *            The context from where the completion request was invoked.
     * @return The {@link PatternRecommendation}s holding information for the
     *         templates to be displayed.
     */
    public ImmutableSet<PatternRecommendation> computeRecommendations(final CompletionTargetVariable targetVariable,
            final IIntelligentCompletionContext context) {
        if (canFindVariableUsage(targetVariable, context) && canFindModel(targetVariable.getType())) {
            updateModel(context.getVariable(), context.getEnclosingMethodsFirstDeclaration());
            return computeRecommendationsForModel(targetVariable.isNeedsConstructor());
        }
        return ImmutableSet.of();
    }

    /**
     * @param targetVariable
     *            The variable on which the completion request was invoked.
     * @param context
     *            The context from where to resolve variable usage.
     * @return True, if a new variable is to be constructed or an existing's
     *         usage could be resolved.
     */
    private boolean canFindVariableUsage(final CompletionTargetVariable targetVariable,
            final IIntelligentCompletionContext context) {
        boolean result = true;
        if (context.getVariable() == null) {
            receiverMethodInvocations = targetVariable.getReceiverCalls();
        } else {
            result = canResolveVariableUsage(context);
        }
        return result;
    }

    /**
     * @param context
     *            The context from where to resolve variable usage.
     * @return True, if a provided {@link IVariableUsageResolver} was able to
     *         resolve the variable usage.
     */
    private boolean canResolveVariableUsage(final IIntelligentCompletionContext context) {
        boolean result = false;
        for (final IVariableUsageResolver resolver : usageResolvers.get()) {
            if (resolver.canResolve(context)) {
                receiverMethodInvocations = resolver.getReceiverMethodInvocations();
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * @return True, if a model for the current receiver type could be found.
     */
    private boolean canFindModel(final ITypeName receiverType) {
        boolean result = false;
        if (callsModelStore.hasModel(receiverType)) {
            model = callsModelStore.getModel(receiverType);
            this.receiverType = receiverType;
            result = true;
        }
        return result;
    }

    /**
     * @param contextVariable
     *            The target variable as given by the context.
     * @param enclosingMethodsFirstDeclaration
     *            The first declaration (i.e. where an overridden method was
     *            first declared) of the method enclosing the variable on which
     *            the completion request was invoked.
     */
    private void updateModel(final Variable contextVariable, final IMethodName enclosingMethodsFirstDeclaration) {
        model.clearEvidence();
        model.setAvailablity(true);
        model.setMethodContext(enclosingMethodsFirstDeclaration);
        model.setObservedMethodCalls(receiverType, receiverMethodInvocations);
        if (negateConstructors(contextVariable)) {
            model.negateConstructors();
        }
        model.updateBeliefs();
    }

    /**
     * @param contextVariable
     *            The target variable as given by the context.
     * @return True, if the patterns should definitely contain no constructors.
     */
    private boolean negateConstructors(final Variable contextVariable) {
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
            if (keepPattern(patternMethods, constructorRequired)) {
                final int percentage = (int) (patternWithProbablity.getSecond().doubleValue() * 100);
                typeRecs.add(PatternRecommendation.create(patternName, patternMethods, percentage));
            }
        }
        return ImmutableSet.copyOf(typeRecs);
    }

    /**
     * @return Most probable pattern names and their probabilities, trimmed to
     *         the size of <code>MAX_PATTERNS</code>.
     */
    private List<Tuple<String, Double>> findMostLikelyPatterns() {
        List<Tuple<String, Double>> patterns = model.getPatternsNode().getPatternsWithProbability();
        patterns = Lists.newArrayList(Iterators.filter(patterns.iterator(), new PatternProbabilityFilter()));
        Collections.sort(patterns, new PatternSorter());
        patterns = patterns.subList(0, Math.min(patterns.size(), MAX_PATTERNS));
        return ImmutableList.copyOf(patterns);
    }

    /**
     * @param patternName
     *            The interal names, used to identify the pattern inside the
     *            pattern store.
     * @return The methods which shall be invoked by the template built from the
     *         given pattern.
     */
    private ImmutableList<IMethodName> getMethodCallsForPattern(final String patternName) {
        final List<IMethodName> recommendedMethods = Lists.newArrayList();
        model.setPattern(patternName);
        model.updateBeliefs();
        for (final Tuple<IMethodName, Double> pair : model.getRecommendedMethodCalls(METHOD_PROBABILITY_THRESHOLD)) {
            recommendedMethods.add(pair.getFirst());
        }
        return ImmutableList.copyOf(recommendedMethods);
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
    private boolean keepPattern(final List<IMethodName> patternMethods, final boolean constructorRequired) {
        return !patternMethods.isEmpty() && (!constructorRequired || patternMethods.get(0).isInit());
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
            final int probabilityOrder = pattern2.getSecond().compareTo(pattern1.getSecond());
            return probabilityOrder == 0 ? pattern1.getFirst().compareTo(pattern2.getFirst()) : probabilityOrder;
        }
    }
}
