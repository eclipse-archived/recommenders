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

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class ModifiersMethodAnalyzer implements IMethodAnalyzer {
    @Override
    public void analyzeMethod(final Entrypoint entrypoint, final MethodDeclaration methodDecl,
            final IProgressMonitor monitor) {
        final IMethod method = entrypoint.getMethod();
        int modifiers = 0;
        if (method.isPrivate()) {
            modifiers |= Modifier.PRIVATE;
        }
        if (method.isProtected()) {
            modifiers |= Modifier.PROTECTED;
        }
        if (method.isPublic()) {
            modifiers |= Modifier.PUBLIC;
        }
        if (method.isAbstract()) {
            modifiers |= Modifier.ABSTRACT;
        }
        if (method.isFinal()) {
            modifiers |= Modifier.FINAL;
        }
        if (method.isStatic()) {
            modifiers |= Modifier.STATIC;
        }
        if (method.isSynchronized()) {
            modifiers |= Modifier.SYNCHRONIZED;
        }
        if (method.isNative()) {
            modifiers |= Modifier.NATIVE;
        }
        methodDecl.modifiers = modifiers;
    }
}
