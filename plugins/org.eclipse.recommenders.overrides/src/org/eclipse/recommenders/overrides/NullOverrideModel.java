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
package org.eclipse.recommenders.overrides;

import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.ImmutableSet;

public class NullOverrideModel implements IOverrideModel {

    public static final NullOverrideModel INSTANCE = new NullOverrideModel();

    @Override
    public void reset() {
    }

    @Override
    public void setObservedMethod(IMethodName method) {
    }

    @Override
    public ITypeName getType() {
        return VmTypeName.NULL;
    }

    @Override
    public ImmutableSet<IMethodName> getKnownMethods() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<String> getKnownPatterns() {
        return ImmutableSet.of();
    }

    @Override
    public List<Recommendation<IMethodName>> recommendOverrides() {
        return Collections.emptyList();
    }
}
