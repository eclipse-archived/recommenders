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
package org.eclipse.recommenders.internal.calls.rcp.store;

import java.io.Closeable;

import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.calls.rcp.IObjectMethodCallsNet;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IModelArchive extends Closeable {

    /**
     * Dummy object returned by a model archive store if no matching archive
     * could be found. Calls to {@link #hasModel(ITypeName)} will always return
     * false.
     */
    public static final IModelArchive NULL = new NullModelArchive();

    public abstract Manifest getManifest();

    public abstract boolean hasModel(final ITypeName name);

    public abstract IObjectMethodCallsNet acquireModel(final ITypeName name);

    public abstract void releaseModel(IObjectMethodCallsNet model);

}