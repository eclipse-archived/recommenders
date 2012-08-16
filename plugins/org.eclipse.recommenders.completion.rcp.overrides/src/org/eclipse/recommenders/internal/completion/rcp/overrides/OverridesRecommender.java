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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.MethodDeclaration;
import org.eclipse.recommenders.internal.utils.codestructs.TypeDeclaration;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class OverridesRecommender {

    private final double MIN_PROBABILITY_THRESHOLD = 0.1d;

    private IModelArchiveStore<IType, ClassOverridesNetwork> modelStore;
    private final JavaElementResolver jdtCache;

    private IType enclosingType;
    private IType supertype;
    private List<OverridesRecommendation> recommendations= Collections.emptyList();
    private ClassOverridesNetwork model;

    private IRecommendersCompletionContext ctx;

    @Inject
    public OverridesRecommender(IModelArchiveStore<IType, ClassOverridesNetwork> modelStore,
            final JavaElementResolver jdtCache) {
        this.modelStore = modelStore;
        this.jdtCache = jdtCache;
    };

    public void startSession(IRecommendersCompletionContext context) throws JavaModelException {
        this.ctx = context;
        if (findEnclosingType() && findSuperclass() && hasModel()) {
            try {
                computeRecommendations();
            } finally {
                releaseModel();
            }
        }
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
        model = modelStore.aquireModel(supertype).orNull();
        return model != null;
    }

    private void computeRecommendations() throws JavaModelException {
        final TypeDeclaration query = computeQuery();
        for (final MethodDeclaration method : query.methods) {
            model.observeMethodNode(method.superDeclaration);
        }
        recommendations = readRecommendations();
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

    private List<OverridesRecommendation> readRecommendations() {
        final List<OverridesRecommendation> res = Lists.newLinkedList();
        for (final Tuple<IMethodName, Double> item : model.getRecommendedMethodOverrides(MIN_PROBABILITY_THRESHOLD, 5)) {
            final IMethodName method = item.getFirst();
            final Double probability = item.getSecond();
            final OverridesRecommendation recommendation = new OverridesRecommendation(method, probability);
            res.add(recommendation);
        }
        return res;
    }

    public List<OverridesRecommendation> getRecommendations() {
        return recommendations;
    }

    private void releaseModel() {
        if (model != null) modelStore.releaseModel(model);
    }

}
