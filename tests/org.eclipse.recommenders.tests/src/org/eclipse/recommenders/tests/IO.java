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
package org.eclipse.recommenders.tests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.recommenders.utils.Throws;

import com.google.common.collect.Sets;

public class IO {
    private final static class JarFileFileFilter implements IOFileFilter {
        @Override
        public boolean accept(final File file) {
            return file.getName().endsWith(".jar");
        }

        @Override
        public boolean accept(final File dir, final String name) {
            return false;
        }
    }

    private final static class AllDirsOnlyOnceFileFilter implements IOFileFilter {
        Set<File> history = Sets.newHashSet();

        @Override
        public boolean accept(final File file) {
            try {
                return history.add(file.getCanonicalFile());
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e);
            }
        }

        @Override
        public boolean accept(final File dir, final String name) {
            return accept(dir);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<File> getAllJarsInDirectoryRecursively(final File directory) {
        // not working with OSX circular directory links
        // return FileUtils.listFiles(directory, new String[] { "jar" },
        // recursively);
        return FileUtils.listFiles(directory, new JarFileFileFilter(), new AllDirsOnlyOnceFileFilter());
    }

}
