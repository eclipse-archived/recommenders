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
package org.eclipse.recommenders.models.rcp;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.models.rcp.Constants.P_REPOSITORY_URL;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.wiring.DummyModelRepository;

import com.google.common.base.Optional;

// just a thin layer around the model archive repository to respond to url changes etc.
public class ModelArchiveProvider {

    // TODO it's null!
    ModelRepository repo = new DummyModelRepository();

    @Inject
    protected void setRepositoryUrl(@Preference(P_REPOSITORY_URL) String newUrl) {
        repo.setRemote(newUrl);
    }

    public Optional<ModelArchiveCoordinate> resolve(ProjectCoordinate coord, String type) {
        return absent();
    }

    public Optional<File> resolveToFile(ProjectCoordinate coord, String type) {
        return absent();
    }

}
