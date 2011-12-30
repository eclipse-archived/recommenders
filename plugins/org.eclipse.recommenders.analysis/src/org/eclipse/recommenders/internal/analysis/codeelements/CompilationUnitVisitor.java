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
package org.eclipse.recommenders.internal.analysis.codeelements;

public abstract class CompilationUnitVisitor {

    public boolean visit(final CompilationUnit compilationUnit) {
        return true;
    }

    public boolean visit(final TypeDeclaration type) {
        return true;
    }

    public boolean visit(final MethodDeclaration method) {
        return true;
    }

    public boolean visit(final ObjectInstanceKey objectInstanceKey) {
        return true;
    }

    public boolean visit(final Variable variable) {
        return true;
    }

    public boolean visit(final ReceiverCallSite receiverCallSite) {
        return true;
    }

    public boolean visit(final ParameterCallSite parameterCallSite) {
        return true;
    }

}
