/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.IProjectModelFacade;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ProjectServices;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;

import com.google.inject.Provider;
import com.google.inject.internal.util.Sets;

@SuppressWarnings("restriction")
public final class CallsAdapter {

    private final ProjectServices projectServices;
    private final Provider<Set<IVariableUsageResolver>> usageResolversProvider;

    public CallsAdapter(final ProjectServices projectServices,
            final Provider<Set<IVariableUsageResolver>> usageResolversProvider) {
        this.projectServices = projectServices;
        this.usageResolversProvider = usageResolversProvider;
    }

    public SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> getProposalsFromSingleMethods(
            final IJavaElementSelection selection, final IField field, final IIntelligentCompletionContext context) {
        try {
            final Map<IMethodName, Tuple<IMethod, Double>> maxProbs = new HashMap<IMethodName, Tuple<IMethod, Double>>();
            final ITypeName fieldType = context.getVariable().type;
            final IProjectModelFacade facade = getModelFacade(field);
            if (!facade.hasModel(fieldType)) {
                return null;
            }
            for (final IMethod method : field.getDeclaringType().getMethods()) {
                final MockedIntelligentCompletionContext methodContext = ContextFactory.createLocalVariableContext(
                        selection, field.getElementName(), fieldType, ElementResolver.toRecMethod(method));
                for (final Tuple<IMethodName, Double> call : computeRecommendations(fieldType,
                        new HashSet<IMethodName>(), true, methodContext, facade)) {
                    if (!maxProbs.containsKey(call.getFirst())
                            || maxProbs.get(call.getFirst()).getSecond() < call.getSecond()) {
                        maxProbs.put(call.getFirst(), Tuple.create(method, call.getSecond()));
                    }
                }
            }
            final SortedSet<Tuple<IMethodName, Tuple<IMethodName, Double>>> sorted = new TreeSet<Tuple<IMethodName, Tuple<IMethodName, Double>>>(
                    new Comparator<Tuple<IMethodName, Tuple<IMethodName, Double>>>() {
                        @Override
                        public int compare(final Tuple<IMethodName, Tuple<IMethodName, Double>> arg0,
                                final Tuple<IMethodName, Tuple<IMethodName, Double>> arg1) {
                            return arg1.getSecond().getSecond().compareTo(arg0.getSecond().getSecond());
                        }
                    });
            for (final Entry<IMethodName, Tuple<IMethod, Double>> methodCall : maxProbs.entrySet()) {
                sorted.add(Tuple.create(methodCall.getKey(), Tuple.create(ElementResolver.toRecMethod(methodCall
                        .getValue().getFirst()), methodCall.getValue().getSecond())));
            }
            return sorted;
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ITypeName getMethodsDeclaringType(final IMethod method) {
        try {
            final String superclassTypeSignature = method.getDeclaringType().getSuperclassTypeSignature();
            if (superclassTypeSignature == null) {
                return null;
            }
            final String superclassTypeName = JavaModelUtil.getResolvedTypeName(superclassTypeSignature,
                    method.getDeclaringType());
            if (superclassTypeName == null) {
                return null;
            }
            final IType supertype = method.getJavaProject().findType(superclassTypeName);
            return ElementResolver.toRecType(supertype);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<IMethodName> resolveCalledMethods(final IIntelligentCompletionContext context) {
        for (final IVariableUsageResolver resolver : usageResolversProvider.get()) {
            if (resolver.canResolve(context)) {
                return resolver.getReceiverMethodInvocations();
            }
        }
        return Sets.newHashSet();
    }

    public static SortedSet<Tuple<IMethodName, Double>> computeRecommendations(final ITypeName typeName,
            final Set<IMethodName> invokedMethods, final boolean negateConstructors,
            final IIntelligentCompletionContext context, final IProjectModelFacade facade) {
        final IObjectMethodCallsNet model = facade.acquireModel(typeName);
        model.clearEvidence();
        model.setMethodContext(context == null ? null : context.getEnclosingMethodsFirstDeclaration());
        model.setObservedMethodCalls(typeName, invokedMethods);
        if (negateConstructors) {
            model.negateConstructors();
        }
        model.updateBeliefs();
        final SortedSet<Tuple<IMethodName, Double>> recommendedMethodCalls = model.getRecommendedMethodCalls(0.01, 5);
        facade.releaseModel(model);
        return recommendedMethodCalls;
    }

    public IProjectModelFacade getModelFacade(final IJavaElement element) {
        final IJavaProject javaProject = element.getJavaProject();
        return projectServices.getModelFacade(javaProject);
    }

}
