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
package org.eclipse.recommenders.rcp.utils.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class TypeDeclarationFinder {

    public static TypeDeclaration find(final CompilationUnit cu, final ITypeName type) {
        return new TypeDeclarationFinder(cu, type).getMatch();
    }

    private TypeDeclaration match = null;

    public boolean matches() {
        return match != null;
    }

    public TypeDeclaration getMatch() {
        return match;
    }

    public TypeDeclarationFinder(final CompilationUnit cuNode, final ITypeName searchedType) {
        cuNode.accept(new ASTVisitor(false) {

            @Override
            public boolean visit(final TypeDeclaration node) {
                final ITypeBinding b = node.resolveBinding();
                final ITypeName name = BindingUtils.toTypeName(b);
                if (name == searchedType) {
                    match = node;
                    return false;
                }
                return true;
            }

            @Override
            public boolean visit(final MethodDeclaration node) {
                return false;
            }
        });
    }
}
