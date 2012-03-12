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
package org.eclipse.recommenders.internal.completion.rcp.templates;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.PatternRecommendation;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

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

/**
 * Computes context-sensitive {@link PatternRecommendation}s.
 */
public final class PatternRecommender {

    private static final int MAX_PATTERNS = 5;
    private static final double PATTERN_PROBABILITY_THRESHOLD = 0.02d;
    private static final double METHOD_PROBABILITY_THRESHOLD = 0.1d;

    private final Provider<Set<IVariableUsageResolver>> usageResolvers;

    private IIntelligentCompletionContext context;
    private Set<IMethodName> receiverMethodInvocations;
    private final ProjectServices projectServices;

    /**
     * @param usageResolvers
     *            A set of resolvers which are able to compute a variable's type and preceding method invocations from a
     *            given context.
     */
    @Inject
    public PatternRecommender(final ProjectServices projectServices,
            final Provider<Set<IVariableUsageResolver>> usageResolvers) {
        this.projectServices = projectServices;
        this.usageResolvers = usageResolvers;
    }

    /**
     * @param targetVariable
     *            The variable information which could be extracted from the completion context.
     * @return The {@link PatternRecommendation}s holding information for the templates to be displayed.
     */
    public ImmutableSet<PatternRecommendation> computeRecommendations(final CompletionTargetVariable targetVariable) {
        final Builder<PatternRecommendation> recommendations = ImmutableSet.builder();
        context = targetVariable.getContext();
        if (canFindVariableUsage(targetVariable)) {
            final ImmutableSet<IObjectMethodCallsNet> modelsForType = findModelsForType(targetVariable.getType());
            for (final IObjectMethodCallsNet model : modelsForType) {
                updateModel(model, targetVariable.needsConstructor());
                recommendations.addAll(computeRecommendationsForModel(targetVariable.needsConstructor(), model));
            }
            releaseModels(modelsForType);
        }
        return recommendations.build();
    }

    private void releaseModels(final ImmutableSet<IObjectMethodCallsNet> models) {
        final IJavaProject javaProject = context.getCompilationUnit().getJavaProject();
        final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
        for (final IObjectMethodCallsNet model : models) {
            modelFacade.releaseModel(model);
        }
    }

    /**
     * @param targetVariable
     *            The variable on which the completion request was invoked.
     * @return True, if a new variable is to be constructed or an existing's usage could be resolved.
     */
    private boolean canFindVariableUsage(final CompletionTargetVariable targetVariable) {
        if (context.getVariable() == null) {
            receiverMethodInvocations = targetVariable.getReceiverCalls();
            return true;
        }
        return canResolveVariableUsage();
    }

    /**
     * @return True, if a provided {@link IVariableUsageResolver} was able to resolve the variable usage.
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
     *            The type for which suitable models should be found. Can be fully qualified and simple.
     * @return Empty set when no model could be found. One-element set when the type is fully qualified. Multiple
     *         elements when the type is simple (i.e. it matches several classes).
     */
    private ImmutableSet<IObjectMethodCallsNet> findModelsForType(final ITypeName receiverType) {
        final Builder<IObjectMethodCallsNet> models = ImmutableSet.builder();
        final IJavaProject javaProject = context.getCompilationUnit().getJavaProject();
        final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
        if (receiverType.getPackage().isDefaultPackage()) {
            final Set<ITypeName> typeNames = modelFacade.findTypesBySimpleName(receiverType);
            models.addAll(acquireModels(modelFacade, typeNames));
        } else if (modelFacade.hasModel(receiverType)) {
            models.add(modelFacade.acquireModel(receiverType));
        }
        return models.build();
    }

    private static Iterable<? extends IObjectMethodCallsNet> acquireModels(final IProjectModelFacade modelFacade,
            final Set<ITypeName> typeNames) {
        final Set<IObjectMethodCallsNet> models = Sets.newHashSet();
        for (final ITypeName typeName : typeNames) {
            if (modelFacade.hasModel(typeName)) {
                models.add(modelFacade.acquireModel(typeName));
            }
        }
        return models;
    }

    /**
     * Updates the model with respect to the context and the observed method invocations on the target variable.
     */
    private void updateModel(final IObjectMethodCallsNet model, final boolean needsConstructor) {
        model.clearEvidence();
        model.setMethodContext(context.getEnclosingMethodsFirstDeclaration());
        model.setObservedMethodCalls(model.getType(), receiverMethodInvocations);
        // TODO check why "negateConstructors()" was needed here
        // if (!needsConstructor &&
        // shallNegateConstructors(context.getVariable())) {
        // model.negateConstructors();
        // }
    }

    /**
     * @param contextVariable
     *            The target variable as given by the context.
     * @return True, if the patterns should definitely contain no constructors.
     */
    private static boolean shallNegateConstructors(final Variable contextVariable) {
        if (contextVariable == null) {
            return false;
        }
        return contextVariable.fuzzyIsParameter() || contextVariable.fuzzyIsDefinedByMethodReturn()
                || contextVariable.isThis();
    }

    /**
     * @param constructorRequired
     *            True, if patterns without constructors should be filtered out.
     * @return The most probable patterns regarding the updated model, limited to the size of <code>MAX_PATTERNS</code>.
     */
    private ImmutableSet<PatternRecommendation> computeRecommendationsForModel(final boolean constructorRequired,
            final IObjectMethodCallsNet model) {
        final Set<PatternRecommendation> typeRecs = Sets.newTreeSet();
        for (final Tuple<String, Double> patternWithProbablity : findMostLikelyPatterns(model)) {
            final String patternName = patternWithProbablity.getFirst();
            final List<IMethodName> patternMethods = getMethodCallsForPattern(patternName, model);
            if (shouldKeepPattern(patternMethods, constructorRequired)) {
                final int percentage = (int) (patternWithProbablity.getSecond().doubleValue() * 100);
                typeRecs.add(new PatternRecommendation(patternName, model.getType(), patternMethods, percentage));
            }
        }
        return ImmutableSet.copyOf(typeRecs);
    }

    /**
     * @return Most probable pattern names and their probabilities, trimmed to the size of <code>MAX_PATTERNS</code>.
     */
    private static ImmutableList<Tuple<String, Double>> findMostLikelyPatterns(final IObjectMethodCallsNet model) {
        List<Tuple<String, Double>> patterns = model.getPatternsWithProbability();
        patterns = Lists.newArrayList(Iterators.filter(patterns.iterator(), new PatternProbabilityFilter()));
        Collections.sort(patterns, new PatternSorter());
        patterns = patterns.subList(0, Math.min(patterns.size(), MAX_PATTERNS));
        return ImmutableList.copyOf(patterns);
    }

    /**
     * @param patternName
     *            The internal pattern name, used to identify the pattern inside the pattern store.
     * @return The methods which shall be invoked by the template built from the given pattern.
     */
    private static ImmutableList<IMethodName> getMethodCallsForPattern(final String patternName,
            final IObjectMethodCallsNet model) {
        final com.google.common.collect.ImmutableList.Builder<IMethodName> recommendedMethods = ImmutableList.builder();
        model.setPattern(patternName);
        for (final Tuple<IMethodName, Double> pair : model.getRecommendedMethodCalls(METHOD_PROBABILITY_THRESHOLD)) {
            recommendedMethods.add(pair.getFirst());
        }
        return recommendedMethods.build();
    }

    /**
     * @param patternMethods
     *            The methods of a given pattern as received from the mode store.
     * @param constructorRequired
     *            True, if patterns without constructors should be filtered out.
     * @return True, if methods exist and they either contain a constructor or a constructor is not required.
     */
    private boolean shouldKeepPattern(final List<IMethodName> patternMethods, final boolean constructorRequired) {
        if (patternMethods.isEmpty() || (constructorRequired && !patternMethods.get(0).isInit())) {
            return false;
        }
        if (constructorRequired || context.getReceiverType() == null) {
            return true;
        }
        final String prefixToken = Preconditions.checkNotNull(context.getPrefixToken());
        for (final IMethodName method : patternMethods) {
            if (StringUtils.startsWithIgnoreCase(method.getName(), prefixToken)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A {@link Predicate} which filters out patterns not surpassing a minimum probability threshold.
     */
    static final class PatternProbabilityFilter implements Predicate<Tuple<String, Double>> {
        @Override
        public boolean apply(final Tuple<String, Double> pattern) {
            return pattern.getSecond().doubleValue() > PATTERN_PROBABILITY_THRESHOLD;
        }
    }

    /**
     * A {@link Comparator} sorting patterns in their probabilities descending order or by their name in case of same
     * probabilities.
     */
    static final class PatternSorter implements Comparator<Tuple<String, Double>>, Serializable {

        private static final long serialVersionUID = -5090432510479153630L;

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
