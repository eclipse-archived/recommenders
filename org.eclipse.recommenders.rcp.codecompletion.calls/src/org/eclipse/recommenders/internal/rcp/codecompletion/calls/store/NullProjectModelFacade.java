/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import java.io.File;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;

import com.google.common.collect.Sets;

public class NullProjectModelFacade implements IProjectModelFacade {

    @Override
    public boolean hasModel(final ITypeName name) {
        return false;
    }

    @Override
    public IObjectMethodCallsNet acquireModel(final ITypeName name) {
        throw Throws.throwIllegalStateException("Trying to acquire a model for unsupported type");
    }

    @Override
    public void releaseModel(final IObjectMethodCallsNet model) {
        throw Throws.throwIllegalStateException("Trying to release a model for unsupported type");
    }

    @Override
    public Set<ITypeName> findTypesBySimpleName(final ITypeName receiverType) {
        return Sets.newHashSet();
    }

    @Override
    public File[] getDependencyLocations() {
        return new File[0];
    }

}
