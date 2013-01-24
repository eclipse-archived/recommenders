/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp;

import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;

public final class JdtCompletionProposal extends InternalCompletionProposal {
    public JdtCompletionProposal(final int kind, final int completionLocation) {
        super(kind, completionLocation);
    }

    @Override
    public void setDeclarationPackageName(final char[] declarationPackageName) {
        super.setDeclarationPackageName(declarationPackageName);
    }

    @Override
    public void setDeclarationTypeName(final char[] declarationTypeName) {
        super.setDeclarationTypeName(declarationTypeName);
    }

    @Override
    public void setParameterPackageNames(final char[][] parameterPackageNames) {
        super.setParameterPackageNames(parameterPackageNames);
    }

    @Override
    public void setParameterTypeNames(final char[][] parameterTypeNames) {
        super.setParameterTypeNames(parameterTypeNames);
    }

    @Override
    public void setPackageName(final char[] packageName) {
        super.setPackageName(packageName);
    }

    @Override
    public void setTypeName(final char[] typeName) {
        super.setTypeName(typeName);
    }
}
