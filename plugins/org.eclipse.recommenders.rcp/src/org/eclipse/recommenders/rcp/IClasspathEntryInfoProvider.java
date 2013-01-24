/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp;

import java.io.Closeable;
import java.io.File;
import java.util.Set;

import com.google.common.base.Optional;

public interface IClasspathEntryInfoProvider extends Closeable {

    Optional<ClasspathEntryInfo> getInfo(final File file);

    Set<File> getFiles();

}
