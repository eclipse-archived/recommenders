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
package org.eclipse.recommenders.utils.rcp.ast;

import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Lists;

public class TypeDeclarationInformationAnalyzer {

    public static TypeDeclarationInformationAnalyzer find(final MethodDeclaration method) {
        return new TypeDeclarationInformationAnalyzer(method);
    }

    private ITypeName superclassType;
    private List<ITypeName> superInterfaceTypes = Lists.newLinkedList();

    public TypeDeclarationInformationAnalyzer(final MethodDeclaration member) {
        ensureIsNotNull(member);
        final ASTNode parent = member.getParent();
        if (parent instanceof TypeDeclaration) {
            final TypeDeclaration type = (TypeDeclaration) parent;
            superclassType = BindingUtils.toTypeName(type.getSuperclassType());
            for (final Type t : (List<Type>) type.superInterfaceTypes()) {
                final ITypeName typeName = BindingUtils.toTypeName(t);
                if (typeName != null) {
                    superInterfaceTypes.add(typeName);
                }
            }

        } else if (parent instanceof AnonymousClassDeclaration) {
            final AnonymousClassDeclaration type = cast(parent);
            final ClassInstanceCreation classInstanceCreationNode = cast(type.getParent());
            final ITypeBinding anonymousType = classInstanceCreationNode.resolveTypeBinding();
            superclassType = BindingUtils.toTypeName(anonymousType.getSuperclass());
            superInterfaceTypes = BindingUtils.toTypeNames(anonymousType.getInterfaces());
        }
    }

    public ITypeName getSuperclass() {
        return superclassType;
    }

    public List<ITypeName> getSuperInterfaces() {
        return superInterfaceTypes;
    }

    public boolean foundSuperclass() {

        return superclassType != null;
    }
}
