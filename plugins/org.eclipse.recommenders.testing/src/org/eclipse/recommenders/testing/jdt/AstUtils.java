/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing.jdt;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.recommenders.utils.Pair;

public class AstUtils {

    public static final String MARKER = "$";
    public static final String MARKER_ESCAPE = "\\$";

    public static CompilationUnit createAst(final String content) {
        final ASTParser p = ASTParser.newParser(AST.JLS3);
        p.setKind(ASTParser.K_COMPILATION_UNIT);
        p.setSource(content.toCharArray());
        return (CompilationUnit) p.createAST(null);
    }

    public ASTNode findNode(final CompilationUnit cu, final int origSourcePosition) {
        return NodeFinder.perform(cu, origSourcePosition, 1);
    }

    public static Pair<CompilationUnit, Set<Integer>> createAstWithMarkers(final String content) {
        final Set<Integer> markers = new TreeSet<>();

        int pos = 0;
        final StringBuilder sb = new StringBuilder(content);
        while ((pos = sb.indexOf(MARKER, pos)) != -1) {
            sb.delete(pos, pos + 1);
            markers.add(pos);
        }

        final CompilationUnit cu = createAst(sb.toString());
        // final IJavaElement javaElement = cu.getJavaElement();
        // final ITypeRoot typeRoot = cu.getTypeRoot();
        // final IType findPrimaryType = typeRoot.findPrimaryType();
        return Pair.newPair(cu, markers);
    }
}
