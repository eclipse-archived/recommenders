/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import static org.apache.commons.lang3.StringUtils.countMatches;

import org.apache.commons.io.FilenameUtils;

public final class PathUtils {

    private PathUtils() {
    }

    public static boolean matchesSuffixPattern(String path, String suffixPattern) {
        int separators = countMatches(suffixPattern, "/") + 1;
        int separatorIndex = path.length();
        while (separators > 0) {
            separatorIndex = path.lastIndexOf('/', separatorIndex - 1);
            separators--;
        }
        String substring = path.substring(separatorIndex + 1);
        return FilenameUtils.wildcardMatch(substring, suffixPattern);
    }
}
