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
package org.eclipse.recommenders.internal.models.rcp;

import static com.google.common.base.Optional.*;
import static org.eclipse.jdt.launching.JavaRuntime.getVMInstall;
import static org.eclipse.recommenders.models.DependencyInfo.PROJECT_NAME;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public final class Dependencies {

    @Inject
    @VisibleForTesting
    public static IWorkspaceRoot workspace;

    public static Optional<DependencyInfo> createJREDependencyInfo(final IJavaProject javaProject) {
        Optional<String> executionEnvironmentId = getExecutionEnvironmentId(javaProject);

        try {
            IVMInstall vmInstall = getVMInstall(javaProject);
            if (vmInstall == null) {
                return absent();
            }
            File javaHome = vmInstall.getInstallLocation();
            Map<String, String> hints = Maps.newHashMap();
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

    public static DependencyInfo createDependencyInfoForProject(final IJavaProject project) {
        File file = workspace.findMember(project.getPath()).getLocation().toFile();
        Map<String, String> hints = Maps.newHashMap();
        hints.put(PROJECT_NAME, project.getElementName());
        DependencyInfo dependencyInfo = new DependencyInfo(file, DependencyType.PROJECT, hints);
        return dependencyInfo;
    }
}
