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
package org.eclipse.recommenders.utils;

import java.io.IOException;

/**
 * An {@code Openable} is a resource that needs to be opened before it can be accessed. Its {@link Openable#open()}
 * method performs all necessary I/O operations that bring the resource in a valid initial state.
 * <p>
 * Note that the constructor of an {@code Openable} instance should not perform any I/O operations or complex object
 * initialization itself.
 * 
 * @see java.io.Closeable
 */
public interface Openable {

    /**
     * Opens this resource. If the resource is already open then invoking this method has no effect.
     * 
     * @throws IOException
     *             if an I/O error occurs
     */
    void open() throws IOException;
}
