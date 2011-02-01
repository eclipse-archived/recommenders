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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisFixture;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaUtils;

import com.google.common.collect.Sets;

public class JREOnlyAnalysisFixture implements IAnalysisFixture {

    private final Set<File> dependencies = Sets.newTreeSet();

    private final Set<File> application = Sets.newTreeSet();

    private final Set<File> javaRuntime = Sets.newTreeSet();;

    public static JREOnlyAnalysisFixture create() {
        final JREOnlyAnalysisFixture res = new JREOnlyAnalysisFixture();
        res.resolveJavaRuntime();
        return res;
    }

    protected JREOnlyAnalysisFixture() {
        // not public - use create methods
    }

    private void resolveJavaRuntime() {
        final File javaHome = SystemUtils.getJavaHome();
        final Collection<File> jars = WalaUtils.getAllJarsInDirectory(javaHome, true);
        javaRuntime.addAll(jars);
    }

    @Override
    public String getName() {
        return SystemUtils.JAVA_VM_NAME + " - Classpath only";
    }

    @Override
    public String getDescription() {
        return SystemUtils.JAVA_VM_INFO;
    }

    @Override
    public Set<File> getJavaRuntime() {
        return javaRuntime;
    }

    @Override
    public Set<File> getApplication() {
        return application;
    }

    @Override
    public Set<File> getDependencies() {
        return dependencies;
    }
}
