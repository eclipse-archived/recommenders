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
package org.eclipse.recommenders.models.rcp.wiring;

import static com.google.common.base.Optional.absent;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.ProjectCoordinate;

import com.google.common.base.Optional;

public final class DummyModelRepository extends ModelRepository {
    @Override
    public void setRemote(String url) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolve(ModelArchiveCoordinate model, IProgressMonitor monitor) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(ModelArchiveCoordinate model, IProgressMonitor monitor) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isCached(ModelArchiveCoordinate coord) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Optional<File> getLocation(ModelArchiveCoordinate coord) {
        // TODO Auto-generated method stub
        return absent();
    }

    @Override
    public ModelArchiveCoordinate[] findModelArchives(ProjectCoordinate projectCoord, String modelType) {
        // TODO Auto-generated method stub
        return new ModelArchiveCoordinate[0];
    }

    @Override
    public Optional<ModelArchiveCoordinate> findBestModelArchive(ProjectCoordinate projectCoord, String modelType) {
        return absent();
    }
}
