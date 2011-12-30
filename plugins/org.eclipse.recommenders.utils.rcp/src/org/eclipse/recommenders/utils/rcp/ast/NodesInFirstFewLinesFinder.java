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

import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.getLineNumberOfNodeEnd;
import static org.eclipse.recommenders.utils.rcp.ast.ASTNodeUtils.getLineNumberOfNodeStart;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.Sets;

public class NodesInFirstFewLinesFinder {

    private final List<ASTNode> matches;

    public boolean matches() {
        return matches != null;
    }

    public List<ASTNode> getMatches() {
        return matches;
    }

    public NodesInFirstFewLinesFinder(final CompilationUnit cuNode, final MethodDeclaration method,
            final int lineNumbers) {
        final Block body = method.getBody();
        final int lineNumberOfNodeStart = getLineNumberOfNodeStart(cuNode, body) + 1;
        final int lineNumberOfNodeEnd = getLineNumberOfNodeEnd(cuNode, body);
        final int proposedEndLineNumber = lineNumberOfNodeStart + lineNumbers;
        final int effectiveNodeEnd = Math.min(lineNumberOfNodeEnd, proposedEndLineNumber);
        final Set<Integer> lines = createRange(lineNumberOfNodeStart, effectiveNodeEnd);
        matches = new NodesInLinesFinder(cuNode, lines).getMatches();
    }

    private Set<Integer> createRange(final int lineNumberOfNodeStart, final int effectiveNodeEnd) {
        final Set<Integer> lines = Sets.newHashSet();
        for (int i = lineNumberOfNodeStart; i < effectiveNodeEnd; i++) {
            lines.add(i);
        }
        return lines;
    }
}
