/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import java.io.Closeable;

import org.eclipse.jdt.core.IJavaProject;

import com.google.common.base.Optional;

public interface IIndexProvider extends Closeable {

    Optional<IProjectTypesIndex> findIndex(IJavaProject project);

    IProjectTypesIndex findOrCreateIndex(IJavaProject project);

}
