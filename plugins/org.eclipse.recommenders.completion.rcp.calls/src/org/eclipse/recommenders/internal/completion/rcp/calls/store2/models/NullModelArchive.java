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
package org.eclipse.recommenders.internal.completion.rcp.calls.store2.models;

import static org.eclipse.recommenders.utils.Throws.throwIllegalStateException;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.utils.names.ITypeName;

public class NullModelArchive<T extends IModel> implements IModelArchive<T> {

    @Override
    public void close() throws IOException {
    }

    // @Override
    // public Manifest getManifest() {
    // return Manifest.NULL;
    // }

    @Override
    public boolean hasModel(final ITypeName name) {
        return false;
    }

    @Override
    public T acquireModel(final ITypeName name) {
        throw throwIllegalStateException("Not allowed to load non existing model. Call hasModel() to check existance.");
    }

    @Override
    public void releaseModel(final IModel model) {
        throw throwIllegalStateException("Can not release model on NullModelArchive.");
    }

    @Override
    public File getFile() {
        throw throwIllegalStateException("Can not call getFile on NullModelArchive.");
    }

    @Override
    public void setFile(final File file) {
    }

    @Override
    public void open() {
    }
}
