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
package org.eclipse.recommenders.rcp.events;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

public class JavaModelEvents {

    private static class CompilationUnitDeltaEvent {

        public final ICompilationUnit compilationUnit;

        public CompilationUnitDeltaEvent(final ICompilationUnit cu) {
            this.compilationUnit = cu;
        }

    }

    public static final class CompilationUnitAdded extends CompilationUnitDeltaEvent {

        public CompilationUnitAdded(final ICompilationUnit cu) {
            super(cu);
        }
    }

    /**
     * Fine-grained event that occurs whenever a compilation unit is changed/edited. Note, this is a very frequent
     * event. If you want to be informed whenever the compilation unit is saved (written to disc) subscribe for the
     * {@link CompilationUnitSaved}.
     */
    public static final class CompilationUnitChanged extends CompilationUnitDeltaEvent {

        public CompilationUnitChanged(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class CompilationUnitSaved extends CompilationUnitDeltaEvent {

        public CompilationUnitSaved(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class CompilationUnitRemoved extends CompilationUnitDeltaEvent {

        public CompilationUnitRemoved(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class JavaProjectClosed {
        public final IJavaProject project;

        public JavaProjectClosed(final IJavaProject javaProject) {
            this.project = javaProject;
        }

    }

    public static final class JavaProjectOpened {

        public final IJavaProject project;

        public JavaProjectOpened(final IJavaProject javaProject) {
            this.project = javaProject;
        }
    }

    public static final class JarPackageFragmentRootAdded {

        public JarPackageFragmentRoot root;

        public JarPackageFragmentRootAdded(final JarPackageFragmentRoot root) {
            this.root = root;
        }
    }

    public static final class JarPackageFragmentRootRemoved {

        public JarPackageFragmentRoot root;

        public JarPackageFragmentRootRemoved(final JarPackageFragmentRoot root) {
            this.root = root;
        }
    }
    // TODO: classpath changed event (add/remove)

}
