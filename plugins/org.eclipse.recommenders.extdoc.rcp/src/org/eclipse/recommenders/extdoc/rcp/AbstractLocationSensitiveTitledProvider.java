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
package org.eclipse.recommenders.extdoc.rcp;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.utils.Throws;

/**
 * Allos subclasses to implement methods for all possible combinations of
 * element types (e.g. field or method) and element locations.
 */
@SuppressWarnings("restriction")
public abstract class AbstractLocationSensitiveTitledProvider extends AbstractTitledProvider {

    @Override
    public final boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    @Override
    public final ProviderUiUpdateJob updateSelection(final IJavaElementSelection selection) {
        if (selection.getElementLocation() == null) {
            return null;
        }

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
            throw Throws.throwUnreachable("invalid element location observed: '%s'. %s",
                    selection.getElementLocation(), selection);
        }
    }

    private ProviderUiUpdateJob updateImportDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IPackageFragment) {
            return updateImportDeclarationSelection(selection, (IPackageFragment) javaElement);
        } else if (javaElement instanceof IType) {
            return updateImportDeclarationSelection(selection, (IType) javaElement);
        } else if (javaElement instanceof ImportDeclaration) {
            return null;
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    protected ProviderUiUpdateJob updateImportDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment javaElement) {
        return null;
    }

    private ProviderUiUpdateJob updateParameterDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateParameterDeclarationSelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IType) {
            return updateParameterDeclarationSelection(selection, (IType) javaElement);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateParameterDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    protected ProviderUiUpdateJob updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local) {
        return null;
    }

    private ProviderUiUpdateJob updateImplementsDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateImplementsDeclarationSelection(selection, (IType) javaElement);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateImplementsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    private ProviderUiUpdateJob updateExtendsDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateExtendsDeclarationSelection(selection, (IType) javaElement);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    private ProviderUiUpdateJob updateTypeDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateTypeDeclarationSelection(selection, (IType) javaElement);
        }
        // TODO: Quick fix, in some cases the AST seems to be broken while
        // defining new fields and methods, so assume "method body" as default
        // location since it tolerates almost any element.
        return updateMethodBodySelection(selection);
        // throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateTypeDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    private ProviderUiUpdateJob updatePackageDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IPackageFragment) {
            return updatePackageDeclarationSelection(selection, (IPackageFragment) javaElement);
        } else if (javaElement instanceof IType) {
            return updatePackageDeclarationSelection(selection, (IType) javaElement);
        }
        throw new IllegalArgumentException(javaElement.getClass().toString());
    }

    protected ProviderUiUpdateJob updatePackageDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    protected ProviderUiUpdateJob updatePackageDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment pkg) {
        return null;
    }

    private ProviderUiUpdateJob updateMethodDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IMethod) {
            return updateMethodDeclarationSelection(selection, (IMethod) javaElement);
        } else if (javaElement instanceof IType) {
            return updateMethodDeclarationSelection(selection, (IType) javaElement);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateMethodDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        return null;
    }

    protected ProviderUiUpdateJob updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    private ProviderUiUpdateJob updateMethodBodySelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateMethodBodySelection(selection, (ILocalVariable) javaElement);
        } else if (javaElement instanceof IField) {
            return updateMethodBodySelection(selection, (IField) javaElement);
        } else if (javaElement instanceof IType) {
            return updateMethodBodySelection(selection, (IType) javaElement);
        } else if (javaElement instanceof IMethod) {
            return updateMethodBodySelection(selection, (IMethod) javaElement);
        } else if (javaElement instanceof IPackageDeclaration || javaElement instanceof IPackageFragment) {
            return null;
        } else if (javaElement instanceof IImportContainer) {
            return null;
        } else if (javaElement instanceof ITypeRoot) {
            return updateTypeDeclarationSelection(selection, ((ITypeRoot) javaElement).findPrimaryType());
        }
        throw Throws.throwIllegalArgumentException("unexpected selection  type: '%s'", selection.getJavaElement());
    }

    protected ProviderUiUpdateJob updateMethodBodySelection(final IJavaElementSelection selection, final ILocalVariable local) {
        return null;
    }

    protected ProviderUiUpdateJob updateMethodBodySelection(final IJavaElementSelection selection, final IField field) {
        return null;
    }

    protected ProviderUiUpdateJob updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method) {
        return null;
    }

    protected ProviderUiUpdateJob updateMethodBodySelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    private ProviderUiUpdateJob updateFieldDeclarationSelection(final IJavaElementSelection selection) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IField) {
            return updateFieldDeclarationSelection(selection, (IField) javaElement);
        } else if (javaElement instanceof IType) {
            return updateFieldDeclarationSelection(selection, (IType) javaElement);
        } else if (javaElement instanceof IMethod) {
            return updateFieldDeclarationSelection(selection, (IMethod) javaElement);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiUpdateJob updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type) {
        return null;
    }

    protected ProviderUiUpdateJob updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field) {
        return null;
    }

    protected ProviderUiUpdateJob updateFieldDeclarationSelection(final IJavaElementSelection selection, final IMethod method) {
        return null;
    }
}
