/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.coordinates.rcp;

import static com.google.common.base.Optional.*;
import static org.eclipse.jdt.launching.JavaRuntime.getVMInstall;
import static org.eclipse.recommenders.coordinates.DependencyInfo.PROJECT_NAME;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.getLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.DependencyType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public final class DependencyInfos {

    private DependencyInfos() {
        // Not meant to be instantiated
    }

    public static Optional<DependencyInfo> createJreDependencyInfo(IJavaProject javaProject) {
        Optional<String> executionEnvironmentId = getExecutionEnvironmentId(javaProject);

        try {
            IVMInstall vmInstall = getVMInstall(javaProject);
            if (vmInstall == null) {
                return absent();
            }
            File javaHome = vmInstall.getInstallLocation();
            Map<String, String> hints = new HashMap<>();
            if (executionEnvironmentId.isPresent()) {
                hints.put(DependencyInfo.EXECUTION_ENVIRONMENT, executionEnvironmentId.get());
            }
            return of(new DependencyInfo(javaHome, DependencyType.JRE, hints));
        } catch (CoreException e) {
            return absent();
        }
    }

    private static Optional<String> getExecutionEnvironmentId(final IJavaProject javaProject) {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    return fromNullable(JavaRuntime.getExecutionEnvironmentId(entry.getPath()));
                }
            }
            return absent();
        } catch (JavaModelException e) {
            return absent();
        }
    }

    public static Optional<DependencyInfo> createJarDependencyInfo(IPackageFragmentRoot pfr) {
        File file = getLocation(pfr).orNull();
        if (file == null) {
            return absent();
        }

        return of(new DependencyInfo(file, DependencyType.JAR));
    }

    public static Optional<DependencyInfo> createProjectDependencyInfo(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        if (project == null) {
            return Optional.absent();
        }

        IPath path = project.getLocation();
        if (path == null) {
            return Optional.absent();
        }

        File file = path.toFile();
        return of(new DependencyInfo(file, DependencyType.PROJECT,
                ImmutableMap.of(PROJECT_NAME, javaProject.getElementName())));
    }
}
