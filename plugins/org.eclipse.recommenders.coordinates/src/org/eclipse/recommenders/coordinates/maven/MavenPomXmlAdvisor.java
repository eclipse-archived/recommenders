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
package org.eclipse.recommenders.coordinates.maven;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.recommenders.coordinates.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.coordinates.DependencyType.PROJECT;
import static org.eclipse.recommenders.utils.IOUtils.closeQuietly;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.recommenders.coordinates.AbstractProjectCoordinateAdvisor;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
                Document model = readModelFromFile(pomfile);
                return extractProjectCoordinateFromModel(model);
            }
        } catch (Exception e) {
            log.error("Could not read pom.xml file of dependency :" + dependencyInfo, e);
            return absent();
        }
    }

    private Document readModelFromFile(File pomfile) throws IOException, ParserConfigurationException, SAXException {
        InputStream pomInputStream = null;
        try {
            pomInputStream = new FileInputStream(pomfile);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBldr = docBuilderFactory.newDocumentBuilder();
            return docBldr.parse(pomInputStream);
        } finally {
            closeQuietly(pomInputStream);
        }
    }

    private Optional<ProjectCoordinate> extractProjectCoordinateFromModel(Document model)
            throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        String groupId = factory.newXPath().evaluate("/project/groupId/text()", model);
        String artifactId = factory.newXPath().evaluate("/project/artifactId/text()", model);
        String version = factory.newXPath().evaluate("/project/version/text()", model);

        if (isNullOrEmpty(groupId)) {
            groupId = factory.newXPath().evaluate("/project/parent/groupId/text()", model);
        }
        if (isNullOrEmpty(version)) {
            version = factory.newXPath().evaluate("/project/parent/version/text()", model);
        }

        if (isNullOrEmpty(groupId) || isNullOrEmpty(artifactId) || isNullOrEmpty(version)) {
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
