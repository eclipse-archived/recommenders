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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class OverriddenMethodsFinder {

    public static Set<IMethodBinding> find(final ASTNode node) {
        return new OverriddenMethodsFinder(node).getOverriddenMethods();
    }

    private final Set<IMethodBinding> overriddenMethods = Sets.newHashSet();

    public Set<IMethodBinding> getOverriddenMethods() {
        return overriddenMethods;
    }

    public OverriddenMethodsFinder(final ASTNode member) {
        ensureIsNotNull(member);
        member.accept(new ASTVisitor(false) {

            @Override
            public boolean visit(final MethodDeclaration node) {
                final IMethodBinding b = node.resolveBinding();
                if (b != null) {
                    final IMethodBinding supermethod = Bindings.findOverriddenMethod(b, true);
                    if (supermethod != null) {
                        overriddenMethods.add(supermethod);
                    }
                }
                return false;
            }
        });
    }
}
