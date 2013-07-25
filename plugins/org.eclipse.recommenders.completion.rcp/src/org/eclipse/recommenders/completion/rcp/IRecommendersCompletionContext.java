/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public interface IRecommendersCompletionContext {

    JavaContentAssistInvocationContext getJavaContext();

    Optional<ASTNode> getCompletionNode();

    Optional<ASTNode> getCompletionNodeParent();

    Optional<IMethod> getEnclosingMethod();

    Optional<IType> getEnclosingType();

    Optional<IJavaElement> getEnclosingElement();

    ICompilationUnit getCompilationUnit();

    CompilationUnit getAST();

    Optional<IType> getExpectedType();

    Optional<String> getExpectedTypeSignature();

    String getPrefix();

    List<IField> getVisibleFields();

    List<ILocalVariable> getVisibleLocals();

    List<IMethod> getVisibleMethods();

    String getReceiverName();

    Optional<String> getReceiverTypeSignature();

    /**
     * returns the (base) type of the variable, i.e.,
     * <ul>
     * <li> {@code MyClass c;} --> {@code MyClass},
     * <li> {@code List<MyClass>} --> {@code List}, and
     * <li> {@code MyClass MyClass[][] c} --> {@code MyClass}.
     * </ul>
     * 
     * See {@link #getReceiverTypeSignature()} if you need the exact type signature including array literals or
     * generics.
     */
    Optional<IType> getReceiverType();

    boolean isCompletionInMethodBody();

    boolean isCompletionInTypeBody();

    int getInvocationOffset();

    Region getReplacementRange();

    IJavaProject getProject();

    Optional<IType> getClosestEnclosingType();

    boolean hasEnclosingElement();

    /**
     * Returns the method that defines an anonymous value, if any. Checks whether the completion was triggered on an
     * method return value and returns this method if possible.
     */
    Optional<IMethodName> getMethodDef();

    /**
     * Returns all completion proposals JDT would have made at the current completion location.
     */
    Map<IJavaCompletionProposal, CompletionProposal> getProposals();

    /**
     * Returns a set of expected types names at the given location.
     * <p>
     * for {@code if($)} the expected type is boolean, for {@code MessageSend} or
     * {@code CompletionOnQualifiedAllocationExpression} it may be any argument that matches all potential methods to
     * invoke at the current position, e.g., new File($) will return{@code String}, {@code File}, or {@code URI} as
     * there are three different constructors taking values of these types.
     */
    Set<ITypeName> getExpectedTypeNames();

    @Beta
    Optional<CompilationUnitDeclaration> getCompliationUnitDeclaration();

    @Beta
    Optional<Scope> getAssistScope();
}
