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
package org.eclipse.recommenders.internal.commons.analysis.entrypoints;

import org.eclipse.recommenders.internal.commons.analysis.newsites.NewSiteReferenceForThis;
import org.eclipse.recommenders.internal.commons.analysis.utils.RecommendersInits;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.impl.AbstractRootMethod;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.TypeReference;

/**
 * A special analysis entry point that adds for each parameter a call to the
 * recommenders-initializer to the {@link FakeRootMethod}.
 */
public class RecommendersEntrypoint extends DefaultEntrypoint {
    /**
     * @see DefaultEntrypoint
     */
    public RecommendersEntrypoint(final IMethod method, final IClassHierarchy cha) {
        super(method, cha);
    }

    public RecommendersEntrypoint(final IMethod method) {
        this(method, method.getDeclaringClass().getClassHierarchy());
    }

    /**
     * Creates allocation sites for all non-primitive parameters. If a new
     * allocation site is created a call to its recommenders-initializer is
     * added to the FakeRootMethod.
     */
    @Override
    protected int makeArgument(final AbstractRootMethod fakeRootMethod, final int valueNum) {
        final TypeReference typeRef = getParameterTypes(valueNum)[0];
        if (typeRef.isPrimitiveType()) {
            return fakeRootMethod.addLocal();
        } else {
            final SSANewInstruction newInstruction = fakeRootMethod.addAllocationWithoutCtor(typeRef);
            /*
             * 'n' might be null if wala doesn't know the type (because of an
             * incomplete classpath)
             */
            if (null != newInstruction) {
                if (!method.isStatic() && valueNum == 0) {
                    addCallToRecommendersInitializer(fakeRootMethod, newInstruction);
                }
                return newInstruction.getDef();
            }
            return -1;
        }
    }

    private void addCallToRecommendersInitializer(final AbstractRootMethod rootMethod, final SSANewInstruction newAlloc) {
        final int local = newAlloc.getDef();
        final TypeReference type = newAlloc.getConcreteType();
        final CallSiteReference make = RecommendersInits.makeRecommendersInit(type);
        rootMethod.addInvocation(new int[] { local }, make);
    }
}
