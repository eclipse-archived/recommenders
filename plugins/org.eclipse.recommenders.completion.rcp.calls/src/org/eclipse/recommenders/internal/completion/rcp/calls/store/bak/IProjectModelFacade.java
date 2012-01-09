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
package org.eclipse.recommenders.internal.completion.rcp.calls.store.bak;

import java.io.File;
import java.util.Set;

import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IProjectModelFacade {

    public static IProjectModelFacade NULL = new NullProjectModelFacade();

    public abstract boolean hasModel(final ITypeName name);

    public abstract IObjectMethodCallsNet acquireModel(final ITypeName name);

    public abstract void releaseModel(final IObjectMethodCallsNet model);

    public abstract Set<ITypeName> findTypesBySimpleName(final ITypeName receiverType);

    public abstract File[] getDependencyLocations();

}