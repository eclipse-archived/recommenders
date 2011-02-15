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
import java.util.SortedSet;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
final class PatternRecommender {

    private static final int MAX_PATTERNS = 5;
    private static final double PATTERN_PROBABILITY_THRESHOLD = 0.02d;
    private static final double METHOD_PROBABILITY_THRESHOLD = 0.1d;

    private final CallsModelStore callsModelStore;
    private final Provider<Set<IVariableUsageResolver>> usageResolvers;
    private ITypeName receiverType;
    private Set<IMethodName> receiverMethodInvocations;
    private ObjectMethodCallsNet model;

    @Inject
    PatternRecommender(final CallsModelStore callsModelStore, final Provider<Set<IVariableUsageResolver>> usageResolvers) {
        this.callsModelStore = callsModelStore;
        this.usageResolvers = usageResolvers;
    }

    public Set<PatternRecommendation> computeRecommendations(final CompletionTargetVariable targetVariable,
            final IIntelligentCompletionContext context) {
        if (canFindVariableUsage(context) && canFindModel()) {
            updateModel(context);
            final Set<PatternRecommendation> recommendations = computeRecommendationsForModel(targetVariable
                    .isNeedsConstructor());
            return recommendations;
        }
        return Collections.emptySet();
    }

    private boolean canFindVariableUsage(final IIntelligentCompletionContext context) {
        boolean result = true;
        if (context.getVariable() == null) {
            receiverType = context.getReceiverType();
            receiverMethodInvocations = Sets.newHashSet();
        } else {
            result = canResolveVariableUsage(context);
        }
        return result;
    }

    private boolean canResolveVariableUsage(final IIntelligentCompletionContext context) {
        boolean result = false;
        for (final IVariableUsageResolver resolver : usageResolvers.get()) {
            if (resolver.canResolve(context)) {
                receiverType = context.getReceiverType();
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
    private boolean canFindModel() {
        boolean result = false;
        if (callsModelStore.hasModel(receiverType)) {
            model = callsModelStore.getModel(receiverType);
            result = true;
        }
        return result;
    }

    private void updateModel(final IIntelligentCompletionContext context) {
        final Variable contextVariable = context.getVariable();
        model.clearEvidence();
        model.setAvailablity(true);
        model.setMethodContext(context.getEnclosingMethodsFirstDeclaration());
        model.setObservedMethodCalls(receiverType, receiverMethodInvocations);
        if (contextVariable != null
                && (contextVariable.fuzzyIsParameter() || contextVariable.fuzzyIsDefinedByMethodReturn())) {
            model.negateConstructors();
        }
        model.updateBeliefs();
    }

    private Set<PatternRecommendation> computeRecommendationsForModel(final boolean needsConstructor) {
        final Set<PatternRecommendation> typeRecs = Sets.newTreeSet();
        for (final Tuple<String, Double> patternWithProbablity : findMostLikelyPatterns()) {
            final String patternName = patternWithProbablity.getFirst();
            final SortedSet<IMethodName> patternMethods = getMethodCallsForPattern(patternName);
            if (keepPattern(patternMethods, needsConstructor)) {
                final int percentage = (int) (patternWithProbablity.getSecond().doubleValue() * 100);
                final PatternRecommendation recommendation = PatternRecommendation.create(patternName, patternMethods,
                        percentage);
                typeRecs.add(recommendation);
            }
        }
        return typeRecs;
    }

    private List<Tuple<String, Double>> findMostLikelyPatterns() {
        List<Tuple<String, Double>> patterns = model.getPatternsNode().getPatternsWithProbability();
        patterns = Lists.newArrayList(Iterators.filter(patterns.iterator(), new PatternProbabilityFilter()));
        Collections.sort(patterns, new PatternSorter());
        return patterns.subList(0, Math.min(patterns.size(), MAX_PATTERNS));
    }

    private SortedSet<IMethodName> getMethodCallsForPattern(final String patternName) {
        final SortedSet<IMethodName> recommendedMethods = Sets.newTreeSet();
        model.setPattern(patternName);
        model.updateBeliefs();
        for (final Tuple<IMethodName, Double> pair : model.getRecommendedMethodCalls(METHOD_PROBABILITY_THRESHOLD)) {
            recommendedMethods.add(pair.getFirst());
        }
        return recommendedMethods;
    }

    private boolean keepPattern(final SortedSet<IMethodName> patternMethods, final boolean needsConstructor) {
        return !patternMethods.isEmpty() && (!needsConstructor || patternMethods.iterator().next().isInit());
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
