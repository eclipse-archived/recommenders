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

import static com.google.common.base.Optional.*;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;

import com.google.common.base.Optional;

/**
 * Advisor that reads the Bundle-SymbolicName from the manifest file and looks up the project coordinate from the model
 * index if possible.
 */
public class ModelIndexBundleSymbolicNameAdvisor extends AbstractProjectCoordinateAdvisor {

    /*
     * Reusing the osgi advisor may be a little bit redundant but it's not too high for the amount of code we save.
     */
    OsgiManifestAdvisor osgi = new OsgiManifestAdvisor();
    IModelIndex indexer;

    public ModelIndexBundleSymbolicNameAdvisor(IModelIndex indexer) {
        this.indexer = indexer;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        ProjectCoordinate osgiPc = osgi.doSuggest(dependencyInfo).orNull();
        if (osgiPc == null) {
            return absent();
        }
        String symbolicName = osgiPc.getArtifactId();
        ProjectCoordinate indexPc = indexer.suggestProjectCoordinateByArtifactId(symbolicName).orNull();
        if (indexPc == null) {
            return absent();
        }
        return of(new ProjectCoordinate(indexPc.getGroupId(), indexPc.getArtifactId(), osgiPc.getVersion()));
    }

    @Override
    protected boolean isApplicable(DependencyType dependencyType) {
        return osgi.isApplicable(dependencyType);
    }
}
