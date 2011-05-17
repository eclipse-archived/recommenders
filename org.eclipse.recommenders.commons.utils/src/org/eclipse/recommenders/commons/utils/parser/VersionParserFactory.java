/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils.parser;

import java.util.LinkedList;
import java.util.List;

public class VersionParserFactory {

    private static final List<VersionParser> knownParser;

    static {
        knownParser = new LinkedList<VersionParser>();

        knownParser.add(new OsgiVersionParser());
        knownParser.add(new MavenVersionParser());
    }

    public static VersionParser getCompatibleParser(final String version) {
        for (final VersionParser parser : knownParser) {
            if (parser.canParse(version)) {
                return parser;
            }
        }

        return null;
    }
}
