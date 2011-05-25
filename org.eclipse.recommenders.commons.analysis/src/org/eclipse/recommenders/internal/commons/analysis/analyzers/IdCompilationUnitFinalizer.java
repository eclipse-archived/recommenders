/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.commons.utils.Fingerprints;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;

import com.ibm.wala.classLoader.IClass;

public class IdCompilationUnitFinalizer implements ICompilationUnitFinalizer {

    @Override
    public void finalizeClass(final CompilationUnit compilationUnit, final IClass exampleClass,
            final IProgressMonitor monitor) {
        setPrimaryTypeFingerprint(compilationUnit);
    }

    private void setPrimaryTypeFingerprint(final CompilationUnit compilationUnit) {
        compilationUnit.id = Fingerprints.sha1(compilationUnit.primaryType.name
                .getIdentifier());
    }

}
