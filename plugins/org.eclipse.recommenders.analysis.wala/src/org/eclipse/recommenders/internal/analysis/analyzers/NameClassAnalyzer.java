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
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Singleton;
import com.ibm.wala.classLoader.IClass;

@Singleton
public class NameClassAnalyzer implements IClassAnalyzer {
    @Override
    public void analyzeClass(final IClass exampleClass, final TypeDeclaration type, final IProgressMonitor monitor) {
        final ITypeName name = WalaNameUtils.wala2recTypeName(exampleClass);
        type.name = name;
    }
}
