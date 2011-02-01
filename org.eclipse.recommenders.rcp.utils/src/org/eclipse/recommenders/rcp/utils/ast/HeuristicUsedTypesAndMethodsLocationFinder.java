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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class HeuristicUsedTypesAndMethodsLocationFinder {

    public static Set<SimpleName> find(final CompilationUnit cu, final Set<ITypeName> expectedTypes,
            final Set<IMethodName> expectedMethods) {
        return new HeuristicUsedTypesAndMethodsLocationFinder(cu, expectedTypes, expectedMethods)
                .getHeuristicSimpleNames();
    }

    private final Set<SimpleName> guesses = Sets.newHashSet();

    public Set<SimpleName> getHeuristicSimpleNames() {
        return guesses;
    }

    final Set<String> names = Sets.newHashSet();

    public HeuristicUsedTypesAndMethodsLocationFinder(final ASTNode member, final Set<ITypeName> expectedTypes,
            final Set<IMethodName> expectedMethods) {
        for (final ITypeName type : expectedTypes) {
            names.add(type.getClassName());
            names.add(Names.vm2srcTypeName(type.getIdentifier()));
        }
        for (final IMethodName method : expectedMethods) {
            names.add(method.getName());
            if (!method.isVoid()) {
                names.add(method.getReturnType().getClassName());
            }
        }
        ensureIsNotNull(member);
        ensureIsNotNull(expectedTypes);
        member.accept(new ASTVisitor(false) {

            @Override
            public boolean visit(final SimpleName node) {
                final IBinding b = node.resolveBinding();
                if (b == null) {
                    final String identifier = node.getIdentifier();
                    if (names.contains(identifier)) {
                        guesses.add(node);
                    }
                }
                return true;
            }
        });
    }
}
