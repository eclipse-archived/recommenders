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
package org.eclipse.recommenders.internal.rcp.models.store;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;

public interface IDependenciesFactory {

    ModelArchiveResolutionJob newResolutionJob(ModelArchiveMetadata meta, String classifier);

    IModelArchive newModelArchive(File location) throws IOException;

}