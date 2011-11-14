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

import static org.eclipse.recommenders.rcp.utils.ast.ASTNodeUtils.getLineNumberOfNodeStart;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NodesInLinesFinder {

    public static NodesInLinesFinder create(final CompilationUnit cuNode, final Iterable<ASTNode> selectedNodes) {
        final Set<Integer> lines = Sets.newHashSet();
        for (final ASTNode node : selectedNodes) {
            final int lineNumber = getLineNumberOfNodeStart(cuNode, node);
            lines.add(lineNumber);
        }
        return new NodesInLinesFinder(cuNode, lines);
    }

    private final List<ASTNode> matches = Lists.newLinkedList();

    public boolean matches() {
        return matches != null;
    }

    public List<ASTNode> getMatches() {
        return matches;
    }

    public NodesInLinesFinder(final CompilationUnit cuNode, final Set<Integer> lines) {
        cuNode.accept(new ASTVisitor(false) {

            @Override
            public boolean preVisit2(final ASTNode node) {
                final int lineNumber = getLineNumberOfNodeStart(cuNode, node);
                if (lines.contains(lineNumber)) {
                    matches.add(node);
                    return false;
                }
                return true;
            }
        });
    }
}
