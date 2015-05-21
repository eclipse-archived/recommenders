/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.constructors;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.annotations.SerializedName;

public final class ConstructorModel {

    @SerializedName("type")
    private ITypeName type;

    @SerializedName("calls")
    private Multiset<IMethodName> calls = HashMultiset.create();

    public ConstructorModel() {
    }

    public ConstructorModel(ITypeName type, Map<IMethodName, Integer> callFrequencies) {
        this.type = type;
        for (Entry<IMethodName, Integer> entry : callFrequencies.entrySet()) {
            calls.add(entry.getKey(), entry.getValue());
        }
    }

    public ITypeName getExpectedType() {
        return type;
    }

    public int getConstructorCallCount(IMethodName method) {
        return calls.count(method);
    }

    public int getConstructorCallTotal() {
        return calls.size();
    }

    public Set<IMethodName> getEntries() {
        return calls.elementSet();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        ConstructorModel that = (ConstructorModel) other;
        return Objects.equals(this.type, that.type) && Objects.equals(this.calls, that.calls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, calls);
    }
}
