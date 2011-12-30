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
package org.eclipse.recommenders.internal.analysis.analyzers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;

import com.google.inject.Singleton;
import com.ibm.wala.classLoader.IClass;

@Singleton
public class EmptySetsClearerCompilationUnitFinalizer implements ICompilationUnitFinalizer {

    @Override
    public void finalizeClass(final CompilationUnit compilationUnit, final IClass exampleClass,
            final IProgressMonitor monitor) {
        compilationUnit.accept(new CompilationUnitVisitor() {

            @Override
            public boolean visit(final TypeDeclaration type) {
                type.clearEmptySets();
                return true;
            }

            @Override
            public boolean visit(final MethodDeclaration method) {
                method.clearEmptySets();
                return true;
            }

            @Override
            public boolean visit(final ObjectInstanceKey objectInstanceKey) {
                objectInstanceKey.clearEmptySets();
                return false;
            }

        });

    }

}
