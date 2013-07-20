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

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.dependencies.DependencyType.JAR;

import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.recommenders.internal.rcp.repo.RepositoryUtils;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.dependencies.DependencyInfo;
import org.eclipse.recommenders.models.dependencies.DependencyType;
import org.eclipse.recommenders.utils.Zips;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class OsgiManifestStrategy extends AbstractStrategy {
    private static final Name BUNDLE_NAME = new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME);
    private static final Name BUNDLE_VERSION = new Attributes.Name(Constants.BUNDLE_VERSION);
    private Logger log = LoggerFactory.getLogger(OsgiManifestStrategy.class);

    @Override
    protected Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dep) {
        try {
            JarFile jar = new JarFile(dep.getFile());
            final Manifest manifest = jar.getManifest();
            Zips.closeQuietly(jar);
            if (manifest == null) {
                return absent();
            }

            Attributes attributes = manifest.getMainAttributes();
            String name = attributes.getValue(BUNDLE_NAME);
            String version = attributes.getValue(BUNDLE_VERSION);
            if (name == null || version == null) {
                return absent();
            }
            String aid = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, name)[0].getValue();
            String gid = RepositoryUtils.guessGroupId(aid);
            return of(new ProjectCoordinate(gid, aid, version));
        } catch (Exception e) {
            log.error("Exception occured while parsing " + dep, e);
        }
        return absent();
    }

    @Override
    public boolean isApplicable(DependencyType type) {
        return JAR == type;
    }
}
