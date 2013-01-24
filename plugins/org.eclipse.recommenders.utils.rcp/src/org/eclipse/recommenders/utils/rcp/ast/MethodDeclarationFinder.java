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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.haveSameNumberOfParameters;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.haveSameParameterTypes;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.sameSimpleName;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class MethodDeclarationFinder {

    public static Set<MethodDeclaration> find(final CompilationUnit cu, final Set<IMethodName> methods) {
        return new MethodDeclarationFinder(cu, methods).getMatches();
    }

    public static Optional<MethodDeclaration> find(final CompilationUnit cu, final IMethodName method) {
        return new MethodDeclarationFinder(cu, Collections.singleton(method)).getMatch();
    }

    private final Set<MethodDeclaration> matches = Sets.newHashSet();

    public boolean matches() {
        return !matches.isEmpty();
    }

    public Optional<MethodDeclaration> getMatch() {
        return fromNullable(Iterables.getFirst(matches, null));
    }

    public Set<MethodDeclaration> getMatches() {
        return matches;
    }

    public MethodDeclarationFinder(final CompilationUnit cuNode, final Set<IMethodName> searchedMethodes) {
        cuNode.accept(new ASTVisitor(false) {

            @SuppressWarnings("unchecked")
            @Override
            public boolean visit(final MethodDeclaration node) {
                for (final IMethodName searchedMethod : searchedMethodes) {
                    if (sameSimpleName(node, searchedMethod) || searchedMethod.isInit() && searchedMethod.isInit()) {
                        final List<SingleVariableDeclaration> jdtParams = node.parameters();
                        final ITypeName[] crParams = searchedMethod.getParameterTypes();
                        if (haveSameNumberOfParameters(jdtParams, crParams)
                                && haveSameParameterTypes(jdtParams, crParams)) {
                            matches.add(node);
                            break;
                        }
                    }
                }
                return true;
            }
        });
    }
}
