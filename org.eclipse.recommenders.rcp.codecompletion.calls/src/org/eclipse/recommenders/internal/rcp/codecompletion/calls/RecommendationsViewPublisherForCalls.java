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

import org.apache.commons.math.util.MathUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.eclipse.recommenders.rcp.IRecommendation;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class RecommendationsViewPublisherForCalls implements IRecommendationsViewContentProvider {

    private static final double MIN_PROBABILITY_THRESHOLD = 0.30d;

    private Multimap<Object, IRecommendation> recommendations;

    private CompilationUnit compilationUnit;

    private MethodDeclaration method;

    private Variable variable;

    private IObjectMethodCallsNet model;

    private final ProjectServices projectServices;

    private ICompilationUnit jdtCompilationUnit;

    @Inject
    public RecommendationsViewPublisherForCalls(final ProjectServices projectServices) {
        this.projectServices = projectServices;
    }

    @Override
    public synchronized void attachRecommendations(final ICompilationUnit jdtCompilationUnit,
            final CompilationUnit recCompilationUnit, final Multimap<Object, IRecommendation> recommendations) {
        this.jdtCompilationUnit = jdtCompilationUnit;
        this.compilationUnit = recCompilationUnit;
        this.recommendations = recommendations;
        computeRecommendations();
    }

    private void computeRecommendations() {
        compilationUnit.accept(new CompilationUnitVisitor() {

            @Override
            public boolean visit(final MethodDeclaration method) {
                RecommendationsViewPublisherForCalls.this.method = method;
                return true;
            }

            @Override
            public boolean visit(final Variable variable) {
                if (findModel(variable.type)) {
                    RecommendationsViewPublisherForCalls.this.variable = variable;
                    computeRecommendationsForObjectInstance();
                    releaseModel();
                }
                return false;
            }

        });

    }

    private boolean findModel(final ITypeName type) {
        final IJavaProject javaProject = jdtCompilationUnit.getJavaProject();
        final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
        if (modelFacade.hasModel(type)) {
            model = modelFacade.acquireModel(type);
        }
        return model != null;
    }

    protected void releaseModel() {
        if (model != null) {
            final IJavaProject javaProject = jdtCompilationUnit.getJavaProject();
            final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
            modelFacade.releaseModel(model);
            model = null;
        }
    }

    private void computeRecommendationsForObjectInstance() {
        model.clearEvidence();
        model.setMethodContext(method.firstDeclaration);
        model.setObservedMethodCalls(variable.type, variable.getReceiverCalls());
        if (variable.isThis() && !method.name.isInit()) {
            model.negateConstructors();
        } else if (variable.fuzzyIsDefinedByMethodReturn() || variable.fuzzyIsParameter()) {
            model.negateConstructors();
        }
        // compute probabilities:
        model.updateBeliefs();
        // get recommendations from net:
        for (final Tuple<IMethodName, Double> t : model.getRecommendedMethodCalls(MIN_PROBABILITY_THRESHOLD, 5)) {
            final IMethodName methodName = t.getFirst();
            final double probablity = MathUtils.round(t.getSecond(), 3);
            final CallsRecommendation recommendation = CallsRecommendation.create(variable, methodName, probablity);
            recommendations.put(variable, recommendation);
        }
    }
}