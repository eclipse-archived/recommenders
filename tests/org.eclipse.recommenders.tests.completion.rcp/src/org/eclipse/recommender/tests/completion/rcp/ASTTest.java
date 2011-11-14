/**
 * Copyright (c) 2010 Darmstadt University of Technology. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 */
package org.eclipse.recommender.tests.completion.rcp;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

public class ASTTest {

    @Test
    public void testBuildAST() throws Exception {
        final AST ast = AST.newAST(AST.JLS3);
        final CompilationUnit cu = ast.newCompilationUnit();
        final TypeDeclaration dec = ast.newTypeDeclaration();
        final MethodDeclaration m = ast.newMethodDeclaration();
        dec.bodyDeclarations().add(m);
        ast.newMethodDeclaration();
    }
}
