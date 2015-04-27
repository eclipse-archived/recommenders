/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import org.eclipse.recommenders.coordinates.AbstractProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.Coordinates;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.coordinates.osgi.OsgiManifestAdvisor;
import org.eclipse.recommenders.models.IModelIndex;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 * Advisor that reads the Bundle-SymbolicName from the manifest file and looks up the project coordinate from the model
 * index if possible.
 */
public class ModelIndexBundleSymbolicNameAdvisor extends AbstractProjectCoordinateAdvisor {

    /*
     * Reusing the OSGI advisor may be a little bit redundant but it's not too high for the amount of code we save.
     */
    private final OsgiManifestAdvisor osgi;
    private final IModelIndex indexer;

    public ModelIndexBundleSymbolicNameAdvisor(IModelIndex indexer) {
        this(indexer, new OsgiManifestAdvisor());
    }

    @VisibleForTesting
    ModelIndexBundleSymbolicNameAdvisor(IModelIndex indexer, OsgiManifestAdvisor osgi) {
        this.indexer = indexer;
        this.osgi = osgi;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        ProjectCoordinate osgiPc = osgi.suggest(dependencyInfo).orNull();
        if (osgiPc == null) {
            return absent();
        }
        String symbolicName = osgiPc.getArtifactId();
        ProjectCoordinate indexPc = indexer.suggestProjectCoordinateByArtifactId(symbolicName).orNull();
        if (indexPc == null) {
            return absent();
        }
        return Coordinates.tryNewProjectCoordinate(indexPc.getGroupId(), indexPc.getArtifactId(),
                canonicalizeVersion(indexPc.getVersion()));
    }

    @Override
    protected boolean isApplicable(DependencyType dependencyType) {
        return osgi.isApplicable(dependencyType);
    }
}
