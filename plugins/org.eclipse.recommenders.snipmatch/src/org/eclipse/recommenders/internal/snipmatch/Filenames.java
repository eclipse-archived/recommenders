/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Filenames {

    private Filenames() {
    }

    /**
     * Splits a filename into a sorted list of filename restrictions. The first entry is the name itself, followed by
     * all possible sub-extensions including their dot.
     *
     * e.g. filename.extension becomes "filename.extension" & ".extension"
     *
     * @return empty list if filename is <code>null</code>
     */
    public static List<String> getFilenameRestrictions(String filename) {
        if (filename == null) {
            return Collections.emptyList();
        }

        List<String> restrictions = new LinkedList<>();

        StringBuilder sb = new StringBuilder();
        String[] split = filename.split("\\.");
        for (int i = split.length - 1; i >= 0; i--) {
            if (i == 0 && split[i].isEmpty()) {
                // filename starts with a dot, ignore first split
                continue;
            }
            sb.insert(0, split[i]);
            if (i > 0) {
                sb.insert(0, '.');
            }
            restrictions.add(0, sb.toString());
        }
        return restrictions;
    }

}
