/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models;

import java.io.Closeable;

import com.google.common.base.Optional;

public interface IModelArchive<K, M> extends Closeable {

    boolean hasModel(final K key);

    Optional<M> acquireModel(final K key);

    void releaseModel(final M value);

    void open();
}
