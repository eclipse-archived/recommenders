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
package org.eclipse.recommenders.internal.rcp.models.archive;

import java.io.Closeable;

public interface IModelFactory<K, M> extends Closeable {

    void open();

    boolean validateModel(K key, M model);

    void passivateModel(K key, M model) throws Exception;

    boolean hasModel(K key);

    M createModel(K key) throws Exception;

    void destroyModel(K key, M obj) throws Exception;

    void activateModel(K key, M model) throws Exception;
}
