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
package org.eclipse.recommenders.internal.commons.analysis.selectors;

import java.util.Set;

import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.utils.ClassUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.MethodUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.RecommendersInits;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaAnalysisUtils;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.types.MethodReference;

/**
 * A {@link MethodTargetSelector} that returns methods only iff the resolved
 * target method is declared in the same class give at selector instance
 * creation. Calls to other methods are not resolved or stubbed.
 */
public class RestrictedDeclaringClassMethodTargetSelector implements MethodTargetSelector {

    private final MethodTargetSelector delegate;

    private final Set<IClass> acceptedDeclaringClasses = Sets.newHashSet();

    private final SSAPropagationCallGraphBuilder builder;
    private Bag<CallSiteReference> reentryCounter = HashBag.newHashBag();

    public RestrictedDeclaringClassMethodTargetSelector(final MethodTargetSelector delegate,
            final IClass restrictedReceiverType, final SSAPropagationCallGraphBuilder builder) {
        this.delegate = delegate;
        this.acceptedDeclaringClasses.add(restrictedReceiverType);
        this.builder = builder;
    }

    @Override
    public IMethod getCalleeTarget(final CGNode caller, final CallSiteReference call, final IClass receiverType) {
        if (Thread.currentThread().isInterrupted()) {
            Throws.throwCancelationException();
        }

        // final boolean receiverThis = isReceiverThis(caller, call,
        // receiverType);
        // experimental(caller, call, receiverType);
        final MethodReference declaredCallTarget = call.getDeclaredTarget();

        if (RecommendersInits.isRecommendersInit(declaredCallTarget)) {
            return RecommendersInits.createRecommendersInit(caller, call, receiverType, builder,
                    new DeclaredNonPrimitiveOrArrayFieldsSelector());

        }
        final IMethod resolvedCalleeTarget = delegate.getCalleeTarget(caller, call, receiverType);
        if (resolvedCalleeTarget == null) {
            return null;
        }
        if (isCallFromNestedClassToEnclosingClass(resolvedCalleeTarget)
                && isCallToCompilerGeneratedStaticAccessMethod(resolvedCalleeTarget)) {
            // we need to expand the set of accepted classes in order to work
            // properly with nested (mostly anonymous) classes.
            acceptedDeclaringClasses.add(resolvedCalleeTarget.getDeclaringClass());
            return resolvedCalleeTarget;
        }
        if (!isResolvedMethodDeclaredInRestrictedType(resolvedCalleeTarget)) {
            if (resolvedCalleeTarget.getReturnType().isPrimitiveType()) {
                return null;
            } else {
                return WalaAnalysisUtils.createStubMethod(resolvedCalleeTarget.getReference(),
                        receiverType == null ? resolvedCalleeTarget.getDeclaringClass() : receiverType);
            }
        }

        // call on this:
        final int count = reentryCounter.count(call);
        if (count > 5) {
            return null;
        }

        reentryCounter.add(call);

        return resolvedCalleeTarget;
    }

    private boolean isCallFromNestedClassToEnclosingClass(final IMethod calleeTarget) {
        for (final IClass acceptedClass : acceptedDeclaringClasses) {
            if (ClassUtils.isNestedClass(acceptedClass, calleeTarget.getDeclaringClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCallToCompilerGeneratedStaticAccessMethod(final IMethod calleeTarget) {
        return MethodUtils.isCompilerGeneratedStaticAccessMethod(calleeTarget);
    }

    private boolean isResolvedMethodDeclaredInRestrictedType(final IMethod calleeTarget) {
        return acceptedDeclaringClasses.contains(calleeTarget.getDeclaringClass());
    }
}
