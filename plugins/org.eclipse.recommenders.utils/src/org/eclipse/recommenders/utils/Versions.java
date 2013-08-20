/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public final class Versions {

    private Versions() {
    }

    public static Version findClosest(final Version startingPoint, Collection<Version> candidates) {

        Collection<Version> closestCandidates = closestCandidates(candidates, new MajorDistance(startingPoint));
        closestCandidates = closestCandidates(closestCandidates, new MinorDistance(startingPoint));
        closestCandidates = closestCandidates(closestCandidates, new PatchDistance(startingPoint));

        return Iterables.getOnlyElement(closestCandidates);
    }

    private static Collection<Version> closestCandidates(Collection<Version> candidates,
            Function<Version, Integer> metric) {
        Collection<Version> closestCandidatesByMajorVersion = newLinkedList();
        int closestDistance = Integer.MAX_VALUE;
        for (Version candidate : candidates) {
            int distance = metric.apply(candidate);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestCandidatesByMajorVersion.clear();
                closestCandidatesByMajorVersion.add(candidate);
            } else if (distance == closestDistance) {
                closestCandidatesByMajorVersion.add(candidate);
            }
        }
        return closestCandidatesByMajorVersion;
    }

    private static class MajorDistance implements Function<Version, Integer> {

        private final Version referencePoint;

        public MajorDistance(Version referencePoint) {
            this.referencePoint = referencePoint;
        }

        @Override
        public Integer apply(Version version) {
            if (version.compareTo(referencePoint) >= 0) {
                return 2 * (version.getMajor() - referencePoint.getMajor()) + 1;
            } else {
                return 2 * (referencePoint.getMajor() - version.getMajor());
            }
        }
    }

    private static class MinorDistance implements Function<Version, Integer> {

        private final Version referencePoint;

        public MinorDistance(Version referencePoint) {
            this.referencePoint = referencePoint;
        }

        @Override
        public Integer apply(Version version) {
            if (version.compareTo(referencePoint) >= 0) {
                return 2 * (version.getMinor() - referencePoint.getMinor()) + 1;
            } else {
                return 2 * (referencePoint.getMinor() - version.getMinor());
            }
        }
    }

    private static class PatchDistance implements Function<Version, Integer> {

        private final Version referencePoint;

        public PatchDistance(Version referencePoint) {
            this.referencePoint = referencePoint;
        }

        @Override
        public Integer apply(Version version) {
            if (version.compareTo(referencePoint) >= 0) {
                return 2 * (version.getPatch() - referencePoint.getPatch()) + 1;
            } else {
                return 2 * (referencePoint.getPatch() - version.getPatch());
            }
        }
    }
}
