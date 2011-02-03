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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.utils.MethodUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class SuperDeclarationMethodAnalyzer implements IMethodAnalyzer {
    @Override
    public void analyzeMethod(final Entrypoint entrypoint, final MethodDeclaration methodDecl,
            final IProgressMonitor monitor) {
        final IMethod method = entrypoint.getMethod();
        if (method.isInit()) {
            return;
        }
        final IMethod superDeclaration = MethodUtils.findSuperDeclaration(method);
        if (superDeclaration != null) {
            methodDecl.superDeclaration = WalaNameUtils.wala2recMethodName(superDeclaration);
        }
    }
}
