/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.recommenders.internal.rcp.JavaModelEventsService;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class JavaModelEventsProviderTest {

    static IWorkspace workspace = ResourcesPlugin.getWorkspace();
    private static JavaModelEventsService sut;

    @BeforeClass
    public static void beforeClass() {
        sut = new JavaModelEventsService(new EventBus(), workspace.getRoot());
        JavaCore.addElementChangedListener(sut);
    }

    @Test
    public void test() throws CoreException {

        JavaProjectFixture f = new JavaProjectFixture(workspace, "model-events");
        IJavaProject project = f.getJavaProject();

        project.close();
        project.open(null);

        Pair<ICompilationUnit, Set<Integer>> m = f.createFileAndParseWithMarkers("public class C {}");
        ICompilationUnit cu = m.getFirst();

        cu.getBuffer().append("// COMMENT");
        cu.save(null, true);
        cu.delete(true, null);

        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
        LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
        for (LibraryLocation element : locations) {
            entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
        }

        // IFolder sourceFolder = project.getFolder("src");
        // sourceFolder.create(false, true, null);

        // add libs to project class path
        project.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

        project.close();
    }
}
