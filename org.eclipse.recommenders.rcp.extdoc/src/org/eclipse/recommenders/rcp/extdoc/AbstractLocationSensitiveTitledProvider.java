/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.PackageDeclaration;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public abstract class AbstractLocationSensitiveTitledProvider extends AbstractTitledProvider {

    @Override
    public final boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    @Override
    public final boolean updateSelection(final IJavaElementSelection selection, final Composite composite) {
        if (selection.getElementLocation() == null) {
            return false;
        }

        switch (selection.getElementLocation()) {
        case METHOD_BODY:
            return updateMethodBodySelection(selection, composite);
        case FIELD_DECLARATION:
            return updateFieldDeclarationSelection(selection, composite);
        case IMPORT_DECLARATION:
            return updateImportDeclarationSelection(selection, composite);
        case METHOD_DECLARATION:
            return updateMethodDeclarationSelection(selection, composite);
        case PARAMETER_DECLARATION:
            return updateParameterDeclarationSelection(selection, composite);
        case PACKAGE_DECLARATION:
            return updatePackageDeclarationSelection(selection, composite);
        case TYPE_DECLARATION:
            return updateTypeDeclarationSelection(selection, composite);
        case EXTENDS_DECLARATION:
            return updateExtendsDeclarationSelection(selection, composite);
        case IMPLEMENTS_DECLARATION:
            return updateImplementsDeclarationSelection(selection, composite);
        default:
            throw Throws.throwUnreachable("invalid element location observed: '%s'. %s",
                    selection.getElementLocation(), selection);
        }
    }

    private boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IPackageFragment) {
            return updateImportDeclarationSelection(selection, (IPackageFragment) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateImportDeclarationSelection(selection, (IType) javaElement, composite);
        } else if (javaElement instanceof ImportDeclaration) {
            return false;
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updateImportDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment javaElement, final Composite composite) {
        return false;
    }

    private boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateParameterDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateParameterDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return false;
    }

    private boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateImplementsDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateImplementsDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return false;
    }

    private boolean updateExtendsDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateExtendsDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    private boolean updateTypeDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateTypeDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateTypeDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateTypeDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updateTypeDeclarationSelection(final IJavaElementSelection selection, final ILocalVariable local,
            final Composite composite) {
        return false;
    }

    private boolean updatePackageDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updatePackageDeclarationSelection(selection, (IPackageFragment) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updatePackageDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(javaElement.getClass().toString());
    }

    protected boolean updatePackageDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updatePackageDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment pkg, final Composite composite) {
        return false;
    }

    private boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IMethod) {
            return updateMethodDeclarationSelection(selection, (IMethod) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateMethodDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method,
            final Composite composite) {
        return false;
    }

    protected boolean updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    private boolean updateMethodBodySelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateMethodBodySelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IField) {
            return updateMethodBodySelection(selection, (IField) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateMethodBodySelection(selection, (IType) javaElement, composite);
        } else if (javaElement instanceof IMethod) {
            return updateMethodBodySelection(selection, (IMethod) javaElement, composite);
        } else if (javaElement instanceof PackageDeclaration) {
            return false;
        } else if (javaElement instanceof ImportContainer) {
            return false;
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local,
            final Composite composite) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IField field,
            final Composite composite) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method,
            final Composite composite) {
        return false;
    }

    protected boolean updateMethodBodySelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    private boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IField) {
            return updateFieldDeclarationSelection(selection, (IField) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateFieldDeclarationSelection(selection, (IType) javaElement, composite);
        } else if (javaElement instanceof IMethod) {
            return updateFieldDeclarationSelection(selection, (IMethod) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return false;
    }

    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field,
            final Composite composite) {
        return false;
    }

    protected boolean updateFieldDeclarationSelection(final IJavaElementSelection selection, final IMethod method,
            final Composite composite) {
        return false;
    }
}
