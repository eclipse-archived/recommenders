/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
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
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.net.InternetDomainName;

public class RepositoryUtils {

    public static Artifact newArtifact(String coordinate) {
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
}
