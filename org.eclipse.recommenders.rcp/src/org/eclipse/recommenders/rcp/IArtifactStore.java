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
package org.eclipse.recommenders.rcp;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;

/**
 * An artifact store collects analysis artifacts created by various
 * {@link ICompilationUnitAnalyzer}s and persists them over Eclipse runs. The
 * store is typically populated during a project build by the
 * {@link RecommendersBuilder} and its associated
 * {@link ICompilationUnitAnalyzer}s.
 */
@Provisional
public interface IArtifactStore {
    /**
     * Loads the requested artifact for the given compilation unit.
     * 
     * @param cu
     *            the compilation
     * @param clazz
     *            the runtime type of the requested artifact
     */
    public <T> T loadArtifact(IJavaElement cu, Class<T> clazz);

    /**
     * Persists the given artifact for the given compilation unit.
     * 
     */
    public <T> void storeArtifact(final IJavaElement element, final T artifact);

    /**
     * Convenience method.
     * 
     * @see #storeArtifact(ICompilationUnit, Object)
     */
    public <T> void storeArtifacts(IJavaElement element, List<T> artifacts);

    /**
     * Removes all artifacts stored for the given compilation unit.
     */
    public void removeArtifacts(final IJavaElement element);

    /**
     * Clears the whole artifact store and discards all artifacts for the given
     * project.
     * 
     */
    public void cleanStore(final IProject project) throws CoreException;

    public boolean hasArtifact(IJavaElement element, Class<?> class1);
}
