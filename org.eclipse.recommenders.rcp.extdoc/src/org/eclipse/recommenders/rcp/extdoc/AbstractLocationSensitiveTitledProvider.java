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
    public final ProviderUiJob updateSelection(final IJavaElementSelection selection, final Composite composite) {
        if (selection.getElementLocation() == null) {
            return null;
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

    private ProviderUiJob updateImportDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IPackageFragment) {
            return updateImportDeclarationSelection(selection, (IPackageFragment) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateImportDeclarationSelection(selection, (IType) javaElement, composite);
        } else if (javaElement instanceof ImportDeclaration) {
            return null;
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateImportDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateImportDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment javaElement, final Composite composite) {
        return null;
    }

    private ProviderUiJob updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateParameterDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateParameterDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final IType type, final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateParameterDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return null;
    }

    private ProviderUiJob updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateImplementsDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateImplementsDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final IType type, final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateImplementsDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return null;
    }

    private ProviderUiJob updateExtendsDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IType) {
            return updateExtendsDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateExtendsDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    private ProviderUiJob updateTypeDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updateTypeDeclarationSelection(selection, (ILocalVariable) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateTypeDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateTypeDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateTypeDeclarationSelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return null;
    }

    private ProviderUiJob updatePackageDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof ILocalVariable) {
            return updatePackageDeclarationSelection(selection, (IPackageFragment) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updatePackageDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(javaElement.getClass().toString());
    }

    protected ProviderUiJob updatePackageDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updatePackageDeclarationSelection(final IJavaElementSelection selection,
            final IPackageFragment pkg, final Composite composite) {
        return null;
    }

    private ProviderUiJob updateMethodDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
        final IJavaElement javaElement = selection.getJavaElement();
        if (javaElement instanceof IMethod) {
            return updateMethodDeclarationSelection(selection, (IMethod) javaElement, composite);
        } else if (javaElement instanceof IType) {
            return updateMethodDeclarationSelection(selection, (IType) javaElement, composite);
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateMethodDeclarationSelection(final IJavaElementSelection selection,
            final IMethod method, final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateMethodDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    private ProviderUiJob updateMethodBodySelection(final IJavaElementSelection selection, final Composite composite) {
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
            return null;
        } else if (javaElement instanceof ImportContainer) {
            return null;
        }
        throw new IllegalArgumentException(selection.toString());
    }

    protected ProviderUiJob updateMethodBodySelection(final IJavaElementSelection selection,
            final ILocalVariable local, final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateMethodBodySelection(final IJavaElementSelection selection, final IField field,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateMethodBodySelection(final IJavaElementSelection selection, final IMethod method,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateMethodBodySelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    private ProviderUiJob updateFieldDeclarationSelection(final IJavaElementSelection selection,
            final Composite composite) {
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

    protected ProviderUiJob updateFieldDeclarationSelection(final IJavaElementSelection selection, final IType type,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateFieldDeclarationSelection(final IJavaElementSelection selection, final IField field,
            final Composite composite) {
        return null;
    }

    protected ProviderUiJob updateFieldDeclarationSelection(final IJavaElementSelection selection,
            final IMethod method, final Composite composite) {
        return null;
    }
}
