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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.utils.IOUtils;

import com.google.common.base.Optional;

public class JREReleaseFileStrategy extends AbstractStrategy {

    @Override
    protected Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dependencyInfo) {
        Optional<FileInputStream> optionalReleaseFileInputStream = readReleaseFileIn(dependencyInfo.getFile());
        if (!optionalReleaseFileInputStream.isPresent()) {
            return absent();
        }
        InputStream releaseFileInputStream = optionalReleaseFileInputStream.get();
        try {
            return extractProjectCoordinateOfReleaseFile(releaseFileInputStream);
        } catch (IOException e) {
            return absent();
        } finally {
            IOUtils.closeQuietly(releaseFileInputStream);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateOfReleaseFile(InputStream releaseFileInputStream)
            throws IOException {
        Properties properties = new Properties();
        properties.load(releaseFileInputStream);
        String version = properties.getProperty("JAVA_VERSION");
        if (version == null) {
            return absent();
        }
        // Replace of " is needed because of the release file structure
        version = version.replace("\"", "");

        ProjectCoordinate projectCoordinate = new ProjectCoordinate("jre", "jre", version);

        return fromNullable(projectCoordinate);
    }

    private Optional<FileInputStream> readReleaseFileIn(File folderPath) {
        try {
            File file = new File(folderPath.getAbsolutePath() + File.separator + "release");
            return fromNullable(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return absent();
        }
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JRE;
    }

}
