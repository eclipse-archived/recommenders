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
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.google.inject.Singleton;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

@Singleton
public class ConstructorSuperDeclarationMethodAnalyzer implements IMethodAnalyzer {

    @Override
    public void analyzeMethod(final Entrypoint entrypoint, final MethodDeclaration methodDecl,
            final IProgressMonitor monitor) {
        final IMethod method = entrypoint.getMethod();
        // filter for constructor calls only
        if (!method.isInit()) {
            return;
        }
        final IClass declaringClass = method.getDeclaringClass();
        final IClass superclass = declaringClass.getSuperclass();
        if (superclass != null) {
            final ITypeName typeName = WalaNameUtils.wala2recTypeName(superclass);
            final IMethodName methodName = VmMethodName.get(typeName.getIdentifier(), "<init>()V");
            methodDecl.firstDeclaration = methodName;
            methodDecl.superDeclaration = methodName;
        }
    }
}
