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
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.ibm.wala.classLoader.IClass;

public class DeclaredSuperclassClassAnalyzer implements IClassAnalyzer {
    @Override
    public void analyzeClass(final IClass exampleClass, final TypeDeclaration type) {
        final IClass superclass = exampleClass.getSuperclass();
        if (superclass != null) {
            type.superclass = WalaNameUtils.wala2recTypeName(superclass);
        }
    }
}
