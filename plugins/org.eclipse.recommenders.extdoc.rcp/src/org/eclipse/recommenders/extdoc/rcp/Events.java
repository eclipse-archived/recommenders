/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.extdoc.rcp;

public final class Events {

    public static final class PackageFragmentSelectionInPackageDeclaration {

    }

    public static final class PackageFragmentSelectionInImportDeclaration {

    }

    public static final class PackageFragmentSelectionInTypeDeclaration {

    }

    public static final class PackageFragmentSelectionInTypeExtendsDeclaration {

    }

    public static final class PackageFragmentSelectionInTypeImplementsDeclaration {

    }

    public static final class PackageFragmentSelectionInFieldDeclaration {

    }

    public static final class PackageFragmentSelectionInFieldInitialization {

    }

    public static final class PackageFragmentSelectionInMethodDeclaration {

    }

    public static final class PackageFragmentSelectionInMethodReturn {

    }

    public static final class PackageFragmentSelectionInMethodThrows {

    }

    public static final class PackageFragmentSelectionInMethodBody {

    }

    public static final class TypeSelectionInImport {

    }

    public static final class TypeSelection {
        // import, type declaration, extends, implements, field declaration..., method_return, method_throws,
        // method_block, localVariable declaration, static method call
    }

    public static final class MethodSelection {
        // import, field initialization, method declaration,
        // (static) method_invocation
    }

    public static final class VariableSelection {
        // field, argument, local
    }

}
