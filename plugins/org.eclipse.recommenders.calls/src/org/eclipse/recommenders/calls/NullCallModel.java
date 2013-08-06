/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.calls;

import static com.google.common.base.Optional.absent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * A fake implementation of {@link ICallModel} that always returns <code>false</code>, absent or empty sets. This class
 * is designed to be used as NULL object to simplify the code and to get around returning <code>null</code> in API
 * methods.
 */
@Beta
public final class NullCallModel implements ICallModel {

    public static NullCallModel INSTANCE = new NullCallModel();
    // Alias
    public static NullCallModel NULL_MODEL = INSTANCE;

    @Override
    public boolean setObservedPattern(String newPatternName) {
        return false;
    }

    @Override
    public boolean setObservedOverrideContext(IMethodName newOverriddenMethod) {
        return false;
    }

    @Override
    public boolean setObservedDefinitionKind(DefinitionKind newKind) {
        return false;
    }

    @Override
    public boolean setObservedDefiningMethod(IMethodName newDefinition) {
        return false;
    }

    @Override
    public boolean setObservedCalls(Set<IMethodName> additionalCalledMethods) {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public Optional<IMethodName> getObservedOverrideContext() {
        return absent();
    }

    @Override
    public Optional<DefinitionKind> getObservedDefinitionKind() {
        return absent();
    }

    @Override
    public Optional<IMethodName> getObservedDefiningMethod() {
        return absent();
    }

    @Override
    public ImmutableSet<IMethodName> getObservedCalls() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<String> getKnownPatterns() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<IMethodName> getKnownOverrideContexts() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<IMethodName> getKnownCalls() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<DefinitionKind> getKnownDefinitionKinds() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<IMethodName> getKnownDefiningMethods() {
        return ImmutableSet.of();
    }

    @Override
    public List<Recommendation<IMethodName>> recommendDefinitions() {
        return Collections.emptyList();
    }

    @Override
    public List<Recommendation<String>> recommendPatterns() {
        return Collections.emptyList();
    }

    @Override
    public List<Recommendation<IMethodName>> recommendCalls() {
        return Collections.emptyList();
    }

    @Override
    public ITypeName getReceiverType() {
        return Constants.NULL_TYPE;
    }
}
