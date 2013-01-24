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
package org.eclipse.recommenders.utils.parser;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;

import org.eclipse.recommenders.utils.Version;

import com.google.common.base.Optional;

public class VersionParserFactory {

    private static final IVersionParser[] knownParser = { new OsgiVersionParser(), new MavenVersionParser() };

    public static Optional<IVersionParser> getCompatibleParser(final String version) {
        for (final IVersionParser parser : knownParser) {
            if (parser.canParse(version)) {
                return of(parser);
            }
        }

        return absent();
    }

    public static Version parse(final String version) {
        final Optional<IVersionParser> opt = getCompatibleParser(version);
        ensureIsTrue(opt.isPresent(), "Given version string '%s' has unknown format and can not be parsed.", version);
        return opt.get().parse(version);
    }
}
