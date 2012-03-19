/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models.archive;

import static com.google.common.base.Optional.absent;

import java.io.IOException;

import org.eclipse.recommenders.internal.rcp.models.IModelArchive;

import com.google.common.base.Optional;

public class NullModelArchive<K, M> implements IModelArchive<K, M> {
    @SuppressWarnings("rawtypes")
    private static final IModelArchive NULL = new NullModelArchive();

    @SuppressWarnings("unchecked")
    public static <K, M> IModelArchive<K, M> empty() {
        return NULL;
    }

    @Override
    public void open() {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasModel(K key) {
        return false;
    }

    @Override
    public Optional<M> acquireModel(K key) {
        return absent();
    }

    @Override
    public void releaseModel(M value) {
    }

}
