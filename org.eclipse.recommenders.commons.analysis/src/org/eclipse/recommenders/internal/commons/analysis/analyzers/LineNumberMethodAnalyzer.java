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

import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

/**
 * Sets the line number of the <b>first bytecode instruction</b> of this method. This is clearly not identical to the
 * line number the method has been declared but a valuable pointer in code when debuging.
 */
public class LineNumberMethodAnalyzer implements IMethodAnalyzer {
    @Override
    public void analyzeMethod(final Entrypoint entrypoint, final MethodDeclaration methodDecl) {
        final IMethod method = entrypoint.getMethod();
        methodDecl.line = method.getLineNumber(0);
    }
}
