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
package org.eclipse.recommenders.internal.completion.rcp.calls.models;

import java.io.Closeable;
import java.io.File;

import org.eclipse.recommenders.utils.names.ITypeName;

public interface IModelArchive<T extends IModel> extends Closeable {

    /**
     * Dummy object returned by a model archive store if no matching archive could be found. Calls to
     * {@link #hasModel(ITypeName)} will always return false.
     */
    @SuppressWarnings("rawtypes")
    public static final IModelArchive NULL = new NullModelArchive();

    // Manifest getManifest();

    boolean hasModel(final ITypeName name);

    T acquireModel(final ITypeName name);

    void releaseModel(final T model);

    File getFile();

    void setFile(final File file);

    void open();

}