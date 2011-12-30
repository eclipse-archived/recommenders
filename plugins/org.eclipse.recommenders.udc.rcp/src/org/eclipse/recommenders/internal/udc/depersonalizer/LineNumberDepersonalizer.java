/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.depersonalizer;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;

public class LineNumberDepersonalizer extends CompilationUnitVisitor implements ICompilationUnitDepersonalizer {

    @Override
    public CompilationUnit depersonalize(final CompilationUnit compilationUnit) {
        compilationUnit.accept(this);
        return compilationUnit;
    }

    @Override
    public boolean visit(final ObjectInstanceKey objectInstanceKey) {
        if (objectInstanceKey.definitionSite != null) {
            objectInstanceKey.definitionSite.lineNumber = -1;
        }
        return super.visit(objectInstanceKey);
    }

    @Override
    public boolean visit(final MethodDeclaration method) {
        method.line = -1;
        return super.visit(method);
    }

    @Override
    public boolean visit(final ParameterCallSite parameterCallSite) {
        parameterCallSite.lineNumber = -1;
        return super.visit(parameterCallSite);
    }

    @Override
    public boolean visit(final ReceiverCallSite receiverCallSite) {
        receiverCallSite.line = -1;
        return super.visit(receiverCallSite);
    }

    @Override
    public boolean visit(final TypeDeclaration type) {
        type.line = -1;
        return super.visit(type);
    }

}
