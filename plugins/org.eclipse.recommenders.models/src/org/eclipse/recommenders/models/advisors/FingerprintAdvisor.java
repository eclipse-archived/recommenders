/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Fingerprints;

import com.google.common.base.Optional;

public class FingerprintAdvisor extends AbstractProjectCoordinateAdvisor {

    private final IModelIndex indexer;

    public FingerprintAdvisor(IModelIndex indexer) {
        this.indexer = indexer;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        String fingerprint = Fingerprints.sha1(dependencyInfo.getFile());
        return indexer.suggestProjectCoordinateByFingerprint(fingerprint);
    }

    @Override
    protected boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JAR;
    }

}
