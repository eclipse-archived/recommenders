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
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.models.DependencyType.JAR;

import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.utils.Artifacts;
import org.eclipse.recommenders.utils.Zips;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class OsgiManifestStrategy extends AbstractStrategy {
    private static final Name BUNDLE_NAME = new Attributes.Name("Bundle-SymbolicName");
    private static final Name BUNDLE_VERSION = new Attributes.Name("Bundle-Version");
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
            int indexOf = name.indexOf(";");
            String aid = name.substring(0, indexOf == -1 ? name.length() : indexOf);
            String gid = Artifacts.guessGroupId(aid);
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
