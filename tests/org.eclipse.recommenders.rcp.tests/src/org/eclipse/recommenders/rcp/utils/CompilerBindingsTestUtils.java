/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - Initial API and implementation
 */
package org.eclipse.recommenders.rcp.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

@SuppressWarnings("restriction")
public class CompilerBindingsTestUtils {

    private static final Method GET_BINDING_RESOLVER = getDeclaredMethod(AST.class, "getBindingResolver");
    private static final Method GET_CORRESPONDING_NODE = getDeclaredMethod(GET_BINDING_RESOLVER.getReturnType(),
            "getCorrespondingNode", ASTNode.class);

    public static org.eclipse.jdt.internal.compiler.ast.ASTNode getCompilerAstNode(CharSequence code) throws Exception {
        JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), "test");
        Pair<CompilationUnit, Set<Integer>> parseResult = fixture.parseWithMarkers(code.toString());
        CompilationUnit cu = parseResult.getFirst();
        int start = Iterables.getOnlyElement(parseResult.getSecond());

        AST ast = cu.getAST();
        ASTNode node = NodeFinder.perform(cu, start, 0);
        return getCorrespondingNode(ast, node);
    }

    private static org.eclipse.jdt.internal.compiler.ast.ASTNode getCorrespondingNode(AST ast, ASTNode domNode)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object bindingResolver = GET_BINDING_RESOLVER.invoke(ast);
        return (org.eclipse.jdt.internal.compiler.ast.ASTNode) GET_CORRESPONDING_NODE.invoke(bindingResolver, domNode);
    }

    private static Method getDeclaredMethod(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
        try {
            Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
