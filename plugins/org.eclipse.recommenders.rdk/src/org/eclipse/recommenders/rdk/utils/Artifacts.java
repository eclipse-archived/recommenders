/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rdk.utils;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.eclipse.recommenders.utils.annotations.Provisional;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.net.InternetDomainName;

@Provisional
public class Artifacts {

    /**
     * groupId:artifactId:packaging:classifier:version.
     */
    public static Artifact asArtifact(String coordinate) {
        return new DefaultArtifact(coordinate);
    }

    public static String asCoordinate(Artifact artifact) {

        // groupId:artifactId:packaging:classifier:version.

        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).append(":");

        if (artifact.getExtension() != null) {
            sb.append(artifact.getExtension()).append(":");
        }

        if (artifact.getClassifier() != null) {
            sb.append(artifact.getClassifier()).append(":");
        }

        sb.append(artifact.getVersion());

        return sb.toString();
    }

    /**
     * @return the repository-relative path to this artifact.
     */
    public static File asFile(Artifact pom) {
        String gid = pom.getGroupId().replace('.', '/');
        String path = String.format("%s/%s/%s/%s", gid, pom.getArtifactId(), pom.getVersion(), toArtifactFileName(pom));
        return new File(path);
    }

    public static String toArtifactFileName(Artifact artifact) {
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        String classifier = artifact.getClassifier();
        String extension = artifact.getExtension();

        StringBuilder sb = new StringBuilder();
        sb.append(artifactId).append('-').append(version);
        if (!isEmpty(classifier)) {
            sb.append('-').append(classifier);
        }
        sb.append('.').append(extension);
        return sb.toString();
    }

    /**
     * 
     * @param reverseDomainName
     *            e.g., org.eclipse.recommenders.rcp
     * @return a best-guess group id that includes the first package under a known public suffix, e.g.,
     *         org.eclipse.recommenders for org.eclipse.recommenders.rcp
     * 
     * @see InternetDomainName#isUnderPublicSuffix()
     */
    public static String guessGroupId(String reverseDomainName) {
        String[] segments = split(reverseDomainName, ".");
        removeSlashes(segments);
        String[] reverse = copyAndReverse(segments);
        InternetDomainName name = InternetDomainName.from(join(reverse, "."));
        if (!name.isUnderPublicSuffix()) {
            return segments[0];
        } else {
            InternetDomainName topPrivateDomain = name.topPrivateDomain();
            int size = topPrivateDomain.parts().size();
            int end = Math.min(segments.length, size + 1);
            return join(subarray(segments, 0, end), ".");
        }
    }

    private static String[] copyAndReverse(String[] segments) {
        String[] reverse = segments.clone();
        reverse(reverse);
        return reverse;
    }

    private static void removeSlashes(String[] segments) {
        for (int i = segments.length; i-- > 0;) {
            segments[i] = replace(segments[i], "/", "");
        }
    }

    public static Artifact pom(Artifact a) {
        DefaultArtifact pom = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), null, "pom", a.getVersion());
        return pom;
    }

    public static Artifact newClassifierAndExtension(Artifact a, String classifier, String extension) {
        DefaultArtifact res = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), classifier, extension,
                a.getVersion());
        return res;
    }

    /**
     * @return an exact copy of the given artifact coordinate with the new extension attribute
     */
    public static Artifact newExtension(Artifact a, String extension) {
        DefaultArtifact res = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), a.getClassifier(), extension,
                a.getVersion());
        return res;
    }

    /**
     * Finds the pom artifact in the given directory and parses its contents. Neither file nor classifier are set
     * (defaults only).
     */
    public static Optional<Artifact> findCoordinate(final File f) {
        try {
            final File pom = computePomFileLocation(f);

            if (!pom.exists()) {
                return absent();
            }
            final Document doc = parsePom(pom);
            return of(extractCoordinateFromPom(doc));
        } catch (final Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    public static Optional<Artifact> findCoordinate(final JarFile jarFile) {
        final File f = new File(jarFile.getName());
        Optional<Artifact> opt = findCoordinate(f);
        if (opt.isPresent()) {
            opt = of(newExtension(opt.get(), "jar"));
        }
        return opt;
    }

    /**
     * @return a handle on the pom file in the current directory.
     */
    public static File computePomFileLocation(final File artifactFile) {
        final String version = artifactFile.getParentFile().getName();
        final String artifactId = artifactFile.getParentFile().getParentFile().getName();
        final String pomFile = String.format("%s-%s.pom", artifactId, version);
        final File pom = new File(artifactFile.getParentFile(), pomFile);
        return pom;
    }

    /**
     * @param pomFile
     *            the pom file - must exist
     * @return artifact containing "groupid:artifactid:version" read from pom file
     * 
     */
    public static Artifact extractCoordinateFromPom(final File pomFile) throws Exception {
        Document doc = parsePom(pomFile);
        final String groupId = getGroupId(doc);
        final String artifactId = getArtifactId(doc);
        final String version = getVersion(doc);
        return new DefaultArtifact(groupId, artifactId, null, version);
    }

    public static Artifact extractCoordinateFromPom(final Document doc) throws XPathExpressionException {
        final String groupId = getGroupId(doc);
        final String artifactId = getArtifactId(doc);
        final String version = getVersion(doc);
        return new DefaultArtifact(groupId, artifactId, null, version);
    }

    private static String getVersion(final Document doc) throws XPathExpressionException {
        final Optional<String> optVersion = find("//project/version/text()", doc);
        if (optVersion.isPresent()) {
            return optVersion.get();
        } else {
            return find("//project/parent/version/text()", doc).get();
        }
    }

    private static String getArtifactId(final Document doc) throws XPathExpressionException {
        return find("//project/artifactId/text()", doc).get();
    }

    private static String getGroupId(final Document doc) throws XPathExpressionException {
        final Optional<String> groupId = find("//project/groupId/text()", doc);
        if (groupId.isPresent()) {
            return groupId.get();
        } else {
            return find("//project/parent/groupId/text()", doc).get();
        }
    }

    private static Document parsePom(final File pom) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(pom);
        return doc;
    }

    private static Optional<String> find(final String string, final Document doc) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final XPathExpression expr = xpath.compile(string);
        final Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (node == null) {
            return Optional.absent();
        } else {
            return Optional.of(node.getTextContent());
        }
    }

    public static Artifact newArtifact(String string) {
        return new DefaultArtifact(string);
    }
}
