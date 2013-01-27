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
package org.eclipse.recommenders.internal.rcp.repo;

import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.net.InternetDomainName;

public class RepositoryUtils {

    public static String EXTENSION_MODELS = "zip"; //$NON-NLS-1$

    public static String CLASSIFIER_COMPLETION_CALLS = "call"; //$NON-NLS-1$
    public static String CLASSIFIER_COMPLETION_OVERRIDES = "ovrd"; //$NON-NLS-1$
    public static String CLASSIFIER_EXTDOC_OVERRIDE = "extdoc-ovrd"; //$NON-NLS-1$
    public static String CLASSIFIER_EXTDOC_OVERRIDE_PATTERNS = "extodc-ovrdp"; //$NON-NLS-1$
    public static String CLASSIFIER_EXTDOC_SELF_CALLS = "extdoc-self"; //$NON-NLS-1$

    public static Artifact newArtifact(String coordinate) {
        return new DefaultArtifact(coordinate);
    }

    public static String asCoordinate(Artifact artifact) {

        // groupId:artifactId:packaging:classifier:version.

        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).append(":"); //$NON-NLS-1$ //$NON-NLS-2$

        if (artifact.getExtension() != null) {
            sb.append(artifact.getExtension()).append(":"); //$NON-NLS-1$
        }

        if (artifact.getClassifier() != null) {
            sb.append(artifact.getClassifier()).append(":"); //$NON-NLS-1$
        }

        sb.append(artifact.getVersion());

        return sb.toString();
    }

    public static String guessGroupId(String reverseDomainName) {
        String[] segments = split(reverseDomainName, "."); //$NON-NLS-1$
        removeSlashes(segments);
        String[] reverse = copyAndReverse(segments);
        InternetDomainName name = InternetDomainName.from(join(reverse, ".")); //$NON-NLS-1$
        if (!name.isUnderPublicSuffix()) {
            return segments[0];
        } else {
            InternetDomainName topPrivateDomain = name.topPrivateDomain();
            int size = topPrivateDomain.parts().size();
            int end = Math.min(segments.length, size + 1);
            return join(subarray(segments, 0, end), "."); //$NON-NLS-1$
        }
    }

    private static String[] copyAndReverse(String[] segments) {
        String[] reverse = segments.clone();
        reverse(reverse);
        return reverse;
    }

    private static void removeSlashes(String[] segments) {
        for (int i = segments.length; i-- > 0;) {
            segments[i] = replace(segments[i], "/", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
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

    public static Artifact pom(Artifact a) {
        DefaultArtifact pom = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), null, "pom", a.getVersion()); //$NON-NLS-1$
        return pom;
    }

    public static Artifact newClassifierAndExtension(Artifact a, String classifier, String extension) {
        DefaultArtifact res = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), classifier, extension,
                a.getVersion());
        return res;
    }
}
