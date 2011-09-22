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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;

/**
 * {@link ICompilationUnitAnalyzer}s do the real work in code recommenders. They
 * are invoked by the recommenders builder whenever a compilation unit changed.
 * The analysis artifact returned by the
 * {@link #analyze(ICompilationUnit, IProgressMonitor)} method is stored in the
 * {@link IArtifactStore} by the recommenders builder automatically.
 */
@Provisional
public interface ICompilationUnitAnalyzer<T> {
    Option<CompilationUnit> analyze(final ICompilationUnit cu, IProgressMonitor monitor);
}
