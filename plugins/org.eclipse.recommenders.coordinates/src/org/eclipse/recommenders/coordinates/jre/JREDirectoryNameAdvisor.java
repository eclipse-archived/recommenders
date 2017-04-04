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
package org.eclipse.recommenders.coordinates.jre;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.utils.Versions.*;

import java.io.File;

import org.eclipse.recommenders.coordinates.AbstractProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.utils.Version;

import com.google.common.base.Optional;

/**
 * This advisor tries to extract the version out of the directory name of java_home.
 */
public class JREDirectoryNameAdvisor extends AbstractProjectCoordinateAdvisor {

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        File directory = dependencyInfo.getFile();

        do {
            String versionString = canonicalizeVersion(directory.getName());
            if (isValidVersion(versionString)) {
                Version version = Version.valueOf(versionString);
                if (version.getMajor() > 1) {
                    // Bring version of Java 9 or later into the 1.x.y form used by earlier JREs.
                    version = new Version(1,  version.getMajor(), version.getMinor());
                }
                return of(new ProjectCoordinate("jre", "jre", version.toString()));
            }
            directory = directory.getParentFile();
        } while (directory != null);

        return absent();
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JRE;
    }
}
