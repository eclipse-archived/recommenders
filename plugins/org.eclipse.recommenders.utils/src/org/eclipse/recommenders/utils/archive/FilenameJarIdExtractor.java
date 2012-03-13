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
package org.eclipse.recommenders.utils.archive;

import java.io.File;
import java.util.jar.JarFile;

public class FilenameJarIdExtractor extends JarIdExtractor {

    @Override
    public void extract(final JarFile jarFile) throws Exception {
        final String path = jarFile.getName();
        final int lastIndexOfSeparator = path.lastIndexOf(File.separator);
        final String filename = path.substring(lastIndexOfSeparator + 1, path.length());
        final int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot > 0) {
            setName(filename.substring(0, lastIndexOfDot));
        } else {
            setName(filename);
        }
    }

}
