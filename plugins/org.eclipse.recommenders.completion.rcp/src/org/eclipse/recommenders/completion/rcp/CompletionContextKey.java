/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.utils.names.ITypeName;

@SuppressWarnings("restriction")
public class CompletionContextKey<T> {

    @Deprecated
    public static final CompletionContextKey<ASTNode> ASSIST_NODE = make();
    @Deprecated
    public static final CompletionContextKey<ASTNode> ASSIST_NODE_PARENT = make();
    @Deprecated
    public static final CompletionContextKey<Scope> ASSIST_SCOPE = make();
    @Deprecated
    public static final CompletionContextKey<CompilationUnitDeclaration> CCTX_COMPILATION_UNIT_DECLARATION = make();

    public static final CompletionContextKey<IAstProvider> AST_PROVIDER = make();
    public static final CompletionContextKey<String> COMPLETION_PREFIX = make();
    public static final CompletionContextKey<IJavaElement> ENCLOSING_ELEMENT = make();
    public static final CompletionContextKey<IMethod> ENCLOSING_METHOD = make();
    public static final CompletionContextKey<IType> ENCLOSING_TYPE = make();
    public static final CompletionContextKey<IType> EXPECTED_TYPE = make();
    public static final CompletionContextKey<Set<ITypeName>> EXPECTED_TYPENAMES = make();
    public static final CompletionContextKey<InternalCompletionContext> INTERNAL_COMPLETIONCONTEXT = make();
    public static final CompletionContextKey<Boolean> IS_COMPLETION_ON_TYPE = make();
    public static final CompletionContextKey<MethodDeclaration> ENCLOSING_AST_METHOD = make();
    public static final CompletionContextKey<IMethod> ENCLOSING_METHOD_FIRST_DECLARATION = make();

    public static final CompletionContextKey<JavaContentAssistInvocationContext> JAVA_CONTENTASSIST_CONTEXT = make();
    public static final CompletionContextKey<Map<IJavaCompletionProposal, CompletionProposal>> JAVA_PROPOSALS = make();
    public static final CompletionContextKey<String> RECEIVER_NAME = make();
    public static final CompletionContextKey<TypeBinding> RECEIVER_TYPEBINDING = make();
    public static final CompletionContextKey<List<IField>> VISIBLE_FIELDS = make();
    public static final CompletionContextKey<List<ILocalVariable>> VISIBLE_LOCALS = make();
    public static final CompletionContextKey<List<IMethod>> VISIBLE_METHODS = make();

    private static <T> CompletionContextKey<T> make() {
        return new CompletionContextKey<T>();
    }
}
