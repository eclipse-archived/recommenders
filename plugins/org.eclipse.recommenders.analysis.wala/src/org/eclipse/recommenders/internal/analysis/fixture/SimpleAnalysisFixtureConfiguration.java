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
package org.eclipse.recommenders.internal.analysis.fixture;

import java.io.File;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.Sets;

public class SimpleAnalysisFixtureConfiguration {

    public String name;
    public String description;
    public SortedSet<File> application;
    public SortedSet<File> dependencies;

    public static SimpleAnalysisFixtureConfiguration create(final String name, final String description,
            final List<File> applicationFiles, final List<File> dependenciesFiles) {
        final SimpleAnalysisFixtureConfiguration res = new SimpleAnalysisFixtureConfiguration();
        res.name = name;
        res.description = description;
        res.application = Sets.newTreeSet(applicationFiles);
        res.dependencies = Sets.newTreeSet(dependenciesFiles);
        return res;
    }
}
