/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.recommenders.calls.rcp.it;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.ProjectCoordinate.UNKNOWN;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer;
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor;
import org.eclipse.recommenders.internal.calls.rcp.CallsRcpPreferences;
import org.eclipse.recommenders.models.BasedTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Stubs {

    private static final class CallModelSpy implements ICallModel {
        private Set<IMethodName> observedCalls = Sets.newHashSet();
        private IMethodName overriddenMethod;
        private IMethodName definedBy;
        private DefinitionKind defType;
        private String patternName;

        @Override
        public ITypeName getReceiverType() {
            return null;
        }

        @Override
        public void reset() {
        }

        @Override
        public boolean setObservedCalls(Set<IMethodName> observedCalls) {
            this.observedCalls = observedCalls;
            return true;
        }

        @Override
        public boolean setObservedOverrideContext(IMethodName overriddenMethod) {
            this.overriddenMethod = overriddenMethod;
            return true;
        }

        @Override
        public boolean setObservedDefiningMethod(IMethodName definedBy) {
            this.definedBy = definedBy;
            return true;
        }

        @Override
        public boolean setObservedDefinitionKind(DefinitionKind defType) {
            this.defType = defType;
            return true;
        }

        @Override
        public boolean setObservedPattern(String patternName) {
            this.patternName = patternName;
            return true;
        }

        @Override
        public Optional<IMethodName> getObservedOverrideContext() {
            return fromNullable(overriddenMethod);
        }

        @Override
        public Optional<IMethodName> getObservedDefiningMethod() {
            return fromNullable(definedBy);
        }

        @Override
        public Optional<DefinitionKind> getObservedDefinitionKind() {
            return fromNullable(defType);
        }

        @Override
        public ImmutableSet<IMethodName> getObservedCalls() {
            return ImmutableSet.copyOf(observedCalls);
        }

        @Override
        public ImmutableSet<IMethodName> getKnownCalls() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<IMethodName> getKnownDefiningMethods() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<IMethodName> getKnownOverrideContexts() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<String> getKnownPatterns() {
            return ImmutableSet.of();
        }

        @Override
        public List<Recommendation<IMethodName>> recommendDefinitions() {
            return ImmutableList.of();
        }

        @Override
        public List<Recommendation<String>> recommendPatterns() {
            return ImmutableList.of();
        }

        @Override
        public List<Recommendation<IMethodName>> recommendCalls() {
            return ImmutableList.of();
        }

        @Override
        public ImmutableSet<DefinitionKind> getKnownDefinitionKinds() {
            return ImmutableSet.of();
        }
    }

    public static MockedIntelligentCompletionProposalComputer newCallComputer() {
        JavaElementResolver jer = new JavaElementResolver();
        IProjectCoordinateProvider pcp = mock(IProjectCoordinateProvider.class);

        when(pcp.toBasedName((IType) anyObject())).thenAnswer(new Answer<Optional<BasedTypeName>>() {

            @Override
            public Optional<BasedTypeName> answer(InvocationOnMock invocation) throws Throwable {
                // wanna refine this later.
                return of(new BasedTypeName(UNKNOWN, VmTypeName.OBJECT));
            }
        });

        // refine later
        when(pcp.toName((IType) anyObject())).thenReturn(VmTypeName.OBJECT);
        when(pcp.toName((IMethod) anyObject())).thenReturn(Optional.of(VmMethodName.NULL));

        ICallModelProvider mp = mock(ICallModelProvider.class);
        when(mp.acquireModel((BasedTypeName) anyObject())).thenReturn(Optional.<ICallModel>of(new CallModelSpy()));
        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcp, mp, new CallsRcpPreferences());
        return new MockedIntelligentCompletionProposalComputer(sut);
    }
}
