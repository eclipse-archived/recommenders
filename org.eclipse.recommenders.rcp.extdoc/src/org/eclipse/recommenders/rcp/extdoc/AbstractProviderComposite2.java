/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.rcp.extdoc;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;

public abstract class AbstractProviderComposite2 extends AbstractProviderComposite {

    /**
     * REVIEW: I think this method is not actually required, right?
     */
    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    @Override
    public final boolean selectionChanged(final IJavaElementSelection selection) {
        if (selection.getInvocationContext() == null) {
            return false;
        }

        hookInitalize(selection);

        switch (selection.getElementLocation()) {
        case METHOD_BODY:
            return updateMethodBodySelection(selection);
        case FIELD_DECLARATION:
            return updateFieldDeclarationSelection(selection);
        case IMPORT_DECLARATION:
            return updateImportDeclarationSelection(selection);
        case METHOD_DECLARATION:
            return updateMethodDeclarationSelection(selection);
        case PARAMETER_DECLARATION:
            return updateParameterDeclarationSelection(selection);
        case PACKAGE_DECLARATION:
            return updatePackageDeclarationSelection(selection);
        case TYPE_DECLARATION:
            return updateTypeDeclarationSelection(selection);
        case EXTENDS_DECLARATION:
            return updateExtendsDeclarationSelection(selection);
        case IMPLEMENTS_DECLARATION:
            return updateImplementsDeclarationSelection(selection);
        default:
            throw throwUnreachable("invalid element location observed: '%s'. %s", selection.getElementLocation(),
                    selection);
        }
    }

    protected void hookInitalize(final IJavaElementSelection selection) {
    }

    private boolean updateImportDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IPackageFragment) {
            return updateImportDeclarationSelection(selection, (IPackageFragment) javaElement);
        } else if (javaElement instanceof IType) {
            return updateImportDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment javaElement) {
        return false;
    }

    private boolean updateParameterDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateParameterDeclarationSelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IType) {
            return updateParameterDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        return false;
    }

    private boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateImplementsDeclarationSelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IType) {
            return updateImplementsDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        return false;
    }

    private boolean updateExtendsDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateExtendsDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    private boolean updateTypeDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateTypeDeclarationSelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IType) {
            return updateTypeDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateTypeDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updateTypeDeclarationSelection(final IJavaElementSelection selection, final ILocalVariable local) {
        return false;
    }

    private boolean updatePackageDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updatePackageDeclarationSelection(selection, (IPackageFragment) javaElement);
        } else if (javaElement instanceof IType) {
            return updatePackageDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updatePackageDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updatePackageDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment pkg) {
        return false;
    }

    private boolean updateMethodDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IMethod) {
            return updateMethodDeclarationSelection(selection, (IMethod) javaElement);
        } else if (javaElement instanceof IType) {
            return updateMethodDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        return false;
    }

    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    private boolean updateMethodBodySelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateMethodBodySelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IField) {
            return updateMethodBodySelection(selection, (IField) javaElement);
        } else if (javaElement instanceof IType) {
            return updateMethodBodySelection(selection, (IType) javaElement);
        } else if (javaElement instanceof IMethod) {
            return updateMethodBodySelection(selection, (IMethod) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    private boolean updateFieldDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IField) {
            return updateFieldDeclarationSelection(selection, (IField) javaElement);
        } else if (javaElement instanceof IType) {
            return updateFieldDeclarationSelection(selection, (IType) javaElement);
        }
        return logUnexpectedJavaElementInSelection(selection);
    }

    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return false;
    }

    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        return false;
    }

    private boolean logUnexpectedJavaElementInSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        System.out.printf("invalid javaelement in selection: '%s'. %s", javaElement.getElementName(), selection);
        return false;
    }
}
