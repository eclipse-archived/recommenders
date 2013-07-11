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
package org.eclipse.recommenders.models;

import java.io.Closeable;

import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.names.IName;

import com.google.common.base.Optional;

/**
 * Minimal API for model providers. Model providers may use local zip-files obtained by, e.g., a {@link ModelRepository}
 * or may directly communicate with a remote web-service.
 * <p>
 * Clients of this API are typically code completion engines and auxiliary documentation providers in IDEs, or
 * evaluation frameworks. The whole framework relies on the usage of {@link IBasedName}, i.e. the ability to assign a
 * base {@link ProjectCoordinate} to the Java class container an {@link IName} originates in.
 **/
public interface IModelProvider<K extends IBasedName<?>, M> extends Closeable, Openable {

    Optional<M> acquireModel(final K key);

    void releaseModel(final M value);

}
