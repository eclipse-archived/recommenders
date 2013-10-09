/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 *    Olav Lenz - Add methods for version strings.
 */
package org.eclipse.recommenders.utils;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public final class Versions {

    private static final Pattern findVersionPattern = Pattern
            .compile("(([1-9][0-9]*)|[0-9])(\\.(([1-9][0-9]*)|[0-9])){0,2}");

    private static final Pattern versionPattern = Pattern.compile("(([1-9][0-9]*)|[0-9])(\\.(([1-9][0-9]*)|[0-9])){2}");

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

    /**
     * Checks if the version has the correct format.
     * 
     * The version must have the following structure: <code>major.minor.micro</code> where major, minor and micro are
     * any number (but w\o leading 0).
     */
    public static boolean isValidVersion(String version) {
        return versionPattern.matcher(version).matches();
    }

    /**
     * Canonicalize a given version string. If it is possible a version with the following structure will be extracted:
     * <code>major.minor.micro</code> where major, minor and micro are any number (but w\o leading 0). If a minor and/or
     * micro version number is missing '.0' will be added for them.
     * <p>
     * 
     * If it is not possible to extract the version the input value is returned.
     */
    public static String canonicalizeVersion(String version) {
        Matcher matcher = findVersionPattern.matcher(version);
        if (matcher.find()) {
            String temp = version.substring(matcher.start(), matcher.end());
            return addMissingVersionPartsIfNecessary(temp);
        }
        return version;
    }

    /**
     * Add '.0' as minor and micro version if they are missing in the string. The method counts the '.' contained in the
     * string and add a '.0' if the number of '.' is smaller than 2 or ".0.0" if the number of '.' is 0.
     */
    private static String addMissingVersionPartsIfNecessary(String version) {
        String temp = version;
        String[] parts = version.split("\\.");
        int missingVersionParts = 3 - parts.length;
        for (int i = 0; i < missingVersionParts; i++) {
            temp += ".0";
        }
        return temp;
    }
}
