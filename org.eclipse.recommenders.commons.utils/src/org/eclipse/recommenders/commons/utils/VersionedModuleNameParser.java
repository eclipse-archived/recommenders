/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedModuleNameParser {

    private static Pattern OSGI_SYMBOLIC_NAME_FILENAME_PATTERN = Pattern.compile("(\\S+?)_(\\S+).jar");

    private static Pattern IVY_OSGI_SYMBOLIC_NAME_FILENAME_PATTERN = Pattern.compile("(\\S+?)-(\\S+).jar");

    public static VersionedModuleName parseFromFile(final File file) {
        return parseFromFileName(file.getName());
    }

    public static VersionedModuleName parseFromFileName(final String fileName) {
        VersionedModuleName res = null;
        res = tryParseOsgiFromFileName(fileName);
        if (res == null) {
            res = tryParseIvyOsgiFromFileName(fileName);
        }
        return res;
    }

    public static VersionedModuleName tryParseOsgiFromFileName(final String fileName) {
        return matchGroups(OSGI_SYMBOLIC_NAME_FILENAME_PATTERN, fileName);
    }

    public static VersionedModuleName tryParseIvyOsgiFromFileName(final String fileName) {
        return matchGroups(IVY_OSGI_SYMBOLIC_NAME_FILENAME_PATTERN, fileName);
    }

    private static VersionedModuleName matchGroups(final Pattern pattern, String fileName) {
        try {
            final Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                final String symbolicName = matcher.group(1);
                String versionGroupString = matcher.group(2);
                final Version version = Version.valueOf(versionGroupString);
                return VersionedModuleName.create(symbolicName, version);
            } else {
                return null;
            }
        } catch (Exception x) {
            return null;
        }
    }
}
