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

public class JavaModelEvents {

    private static class CompilationUnitDeltaEvent {

        public final ICompilationUnit compilationUnit;

        public CompilationUnitDeltaEvent(final ICompilationUnit cu) {
            this.compilationUnit = cu;
        }

    }

    public static final class CompilationUnitAddedEvent extends CompilationUnitDeltaEvent {

        public CompilationUnitAddedEvent(final ICompilationUnit cu) {
            super(cu);
        }
    }

    /**
     * Fine-grained event that occurs whenever a compilation unit is changed/edited. Note, this is a very frequent
     * event. If you want to be informed whenever the compilation unit is saved (written to disc) subscribe for the
     * {@link CompilationUnitSavedEvent}.
     */
    public static final class CompilationUnitChangedEvent extends CompilationUnitDeltaEvent {

        public CompilationUnitChangedEvent(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class CompilationUnitSavedEvent extends CompilationUnitDeltaEvent {

        public CompilationUnitSavedEvent(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class CompilationUnitRemovedEvent extends CompilationUnitDeltaEvent {

        public CompilationUnitRemovedEvent(final ICompilationUnit cu) {
            super(cu);
        }
    }

    public static final class JavaProjectClosedEvent {
        public final IJavaProject project;

        public JavaProjectClosedEvent(final IJavaProject javaProject) {
            this.project = javaProject;
        }

    }

    public static final class JavaProjectOpenedEvent {

        public final IJavaProject project;

        public JavaProjectOpenedEvent(final IJavaProject javaProject) {
            this.project = javaProject;
        }

    }

}
