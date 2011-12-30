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

import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeReference;

public class NameDepersonalizer extends CompilationUnitVisitor implements ICompilationUnitDepersonalizer {

    @Override
    public CompilationUnit depersonalize(final CompilationUnit compilationUnit) {
        compilationUnit.accept(this);
        return compilationUnit;
    }

    @Override
    public boolean visit(final ObjectInstanceKey objectInstanceKey) {
        if (objectInstanceKey.definitionSite != null) {
            objectInstanceKey.definitionSite.definedByField = null;
        }
        return super.visit(objectInstanceKey);
    }

    @Override
    public boolean visit(final CompilationUnit compilationUnit) {
        compilationUnit.name = null;
        for (final TypeReference ref : compilationUnit.imports) {
            ref.name = null;
        }
        return super.visit(compilationUnit);
    }

    @Override
    public boolean visit(final TypeDeclaration type) {
        type.name = null;
        type.fields = null;
        return super.visit(type);
    }

    @Override
    public boolean visit(final MethodDeclaration method) {
        method.name = null;
        for (final ObjectInstanceKey key : method.objects.toArray(new ObjectInstanceKey[0])) {
            key.names = null;
            key.definitionSite = null;
        }
        return super.visit(method);
    }

    @Override
    public boolean visit(final ParameterCallSite parameterCallSite) {
        parameterCallSite.sourceMethod = null;
        parameterCallSite.targetMethod = null;
        parameterCallSite.argumentName = null;
        return super.visit(parameterCallSite);
    }

    @Override
    public boolean visit(final ReceiverCallSite receiverCallSite) {
        receiverCallSite.sourceMethod = null;
        receiverCallSite.receiver = null;
        return super.visit(receiverCallSite);
    }
}
