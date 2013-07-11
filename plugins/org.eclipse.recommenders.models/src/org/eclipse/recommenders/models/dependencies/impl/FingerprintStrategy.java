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
package org.eclipse.recommenders.models.dependencies.impl;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.utils.Fingerprints;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;

public class FingerprintStrategy extends AbstractStrategy {

    private final SimpleIndexSearcher indexer;

    public FingerprintStrategy(SimpleIndexSearcher indexer) {
        this.indexer = indexer;
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JAR;
    }

    @Override
    protected Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dependencyInfo) {
        String fingerprint = Fingerprints.sha1(dependencyInfo.getFile());
        indexer.open();
        Optional<String> optionalCoordinateString = indexer.searchByFingerprint(fingerprint);
        indexer.close();
        return extractProjectCoordinate(optionalCoordinateString);
    }

    private Optional<ProjectCoordinate> extractProjectCoordinate(Optional<String> optionalCoordinateString) {
        if (!optionalCoordinateString.isPresent()){
            return absent();
        }        
        try {
            DefaultArtifact artifact = new DefaultArtifact(optionalCoordinateString.get());
            return fromNullable(new ProjectCoordinate(artifact.getGroupId(), artifact.getArtifactId(),
                    artifact.getVersion()));
        } catch (IllegalArgumentException e) {
            return absent();
        }
    }


}
