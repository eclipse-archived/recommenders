/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.regex.Pattern;

public class PackageTester {
    public PackageTester(final String[] includes, final String[] excludes) {
        super();
        setIncludes(includes);
        setExcludes(excludes);
    }

    public PackageTester() {
    }

    Pattern[] includes = new Pattern[0], excludes = new Pattern[0];

    private boolean matches(final String text, final Pattern[] patterns) {
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    private Pattern[] createPatterns(final String[] expressions) {
        final Pattern[] pattern = new Pattern[expressions.length];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Pattern.compile(expressions[i]);
        }
        return pattern;
    }

    public void setIncludes(final String[] expressions) {
        includes = createPatterns(expressions);
    }

    public void setExcludes(final String[] expressions) {
        excludes = createPatterns(expressions);
    }

    public boolean matches(final String text) {
        if (matches(text, excludes)) {
            return false;
        }
        if (matches(text, includes)) {
            return true;
        }
        return false;
    }
}