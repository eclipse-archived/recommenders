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

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.PROJECT;
import static org.eclipse.recommenders.utils.IOUtils.closeQuietly;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;

public class MavenPomXmlAdvisor extends AbstractProjectCoordinateAdvisor {

    private Logger log = LoggerFactory.getLogger(MavenPomXmlAdvisor.class);

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        try {
            File pomfile = new File(dependencyInfo.getFile(), "pom.xml");
            if (!pomfile.exists()) {
                return absent();
            } else {
                Model model = readModelFromFile(pomfile);
                return extractProjectCoordinateFromModel(model);
            }
        } catch (Exception e) {
            log.error("Could not read pom.xml file of dependency :" + dependencyInfo, e);
            return absent();
        }
    }

    private Model readModelFromFile(File pomfile) throws FileNotFoundException, IOException, XmlPullParserException {
        InputStreamReader pomInputStream = null;
        try {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            pomInputStream = new InputStreamReader(new FileInputStream(pomfile), detectFileEncoding(pomfile));
            return mavenReader.read(pomInputStream);
        } finally {
            closeQuietly(pomInputStream);
        }
    }

    private Charset detectFileEncoding(File file) throws IOException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int first = input.read();
            if (first == 0xEF) {
                // look for the UTF-8 Byte Order Mark (BOM)
                int second = input.read();
                int third = input.read();
                if (second == 0xBB && third == 0xBF) {
                    return Charsets.UTF_8;
                }
            } else if (first == 0xFE) {
                // look for the UTF-16 BOM
                if (input.read() == 0xFF) {
                    return Charsets.UTF_16BE;
                }
            } else if (first == 0xFF) {
                if (input.read() == 0xFE) {
                    return Charsets.UTF_16LE;
                }
            }
        } finally {
            closeQuietly(input);
        }
        return Charset.defaultCharset();
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateFromModel(Model model) {
        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        String version = model.getVersion();

        Parent parent = model.getParent();
        if (parent != null) {
            if (groupId == null) {
                groupId = parent.getGroupId();
            }
            if (version == null) {
                version = parent.getVersion();
            }
        }

        if (groupId == null || artifactId == null || version == null) {
            return absent();
        }
        if (containsPropertyReference(groupId) || containsPropertyReference(artifactId)
                || containsPropertyReference(version)) {
            return absent();
        }

        int indexOf = version.indexOf('-');
        version = version.substring(0, indexOf == -1 ? version.length() : indexOf);
        return tryNewProjectCoordinate(groupId, artifactId, canonicalizeVersion(version));
    }

    private boolean containsPropertyReference(String string) {
        return string.contains("$");
    }

    @Override
    public boolean isApplicable(DependencyType type) {
        return PROJECT == type;
    }
}
