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

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;

import com.google.inject.Singleton;
import com.ibm.wala.classLoader.IClass;

@Singleton
public class ThisObjectInstanceKeyCompilationUnitFinalizer implements ICompilationUnitFinalizer {
    @Override
    public void finalizeClass(final CompilationUnit compilationUnit, final IClass exampleClass,
            final IProgressMonitor monitor) {
        recursiveRemoveWalaExceptions(compilationUnit.primaryType);
    }

    private void recursiveRemoveWalaExceptions(final TypeDeclaration type) {
        for (final TypeDeclaration nested : type.memberTypes) {
            recursiveRemoveWalaExceptions(nested);
        }
        //
        finalizeTypeDeclaration(type);
    }

    public void finalizeTypeDeclaration(final TypeDeclaration type) {
        for (final MethodDeclaration method : type.methods) {
            finalizeMethodDeclaration(method, type);
        }
    }

    public void finalizeMethodDeclaration(final MethodDeclaration method, final TypeDeclaration type) {
        for (final ObjectInstanceKey value : method.objects) {
            if (value.type == type.name) {
                value.type = type.superclass;
                for (final Iterator<ReceiverCallSite> it = value.receiverCallSites.iterator(); it.hasNext();) {
                    final ReceiverCallSite next = it.next();
                    if (next.targetMethod.getDeclaringType() == type.name) {
                        it.remove();
                    }
                }
            }
        }
    }
}
