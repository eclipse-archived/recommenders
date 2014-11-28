/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
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
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@SuppressWarnings({ "restriction", "rawtypes" })
public interface IRecommendersCompletionContext {

    Optional<CompilationUnit> getAST();

    Optional<IType> getClosestEnclosingType();

    ICompilationUnit getCompilationUnit();

    Optional<ASTNode> getCompletionNode();

    Optional<ASTNode> getCompletionNodeParent();

    Optional<IJavaElement> getEnclosingElement();

    Optional<IMethod> getEnclosingMethod();

    Optional<IType> getEnclosingType();

    boolean hasEnclosingElement();

    boolean isCompletionInMethodBody();

    boolean isCompletionInTypeBody();

    Optional<IType> getExpectedType();

    /**
     * Returns a set of expected types names at the given location.
     * <p>
     * for {@code if($)} the expected type is boolean, for {@code MessageSend} or
     * {@code CompletionOnQualifiedAllocationExpression} it may be any argument that matches all potential methods to
     * invoke at the current position, e.g., new File($) will return{@code String}, {@code File}, or {@code URI} as
     * there are three different constructors taking values of these types.
     */
    Set<ITypeName> getExpectedTypeNames();

    Optional<String> getExpectedTypeSignature();

    int getInvocationOffset();

    JavaContentAssistInvocationContext getJavaContext();

    /**
     * Returns the method that defines an anonymous value, if any. Checks whether the completion was triggered on an
     * method return value and returns this method if possible.
     */
    Optional<IMethodName> getMethodDef();

    String getPrefix();

    IJavaProject getProject();

    /**
     * Returns all completion proposals JDT would have made at the current completion location.
     */
    Map<IJavaCompletionProposal, CompletionProposal> getProposals();

    String getReceiverName();

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

    Optional<String> getReceiverTypeSignature();

    Region getReplacementRange();

    List<IField> getVisibleFields();

    List<ILocalVariable> getVisibleLocals();

    List<IMethod> getVisibleMethods();

    /**
     * Returns the value stored in this context under the given key - if any.
     *
     * @param key
     *            the class that get's mapped to a string to build the actual key
     */
    <T> Optional<T> get(CompletionContextKey<T> key);

    /**
     * Returns the value stored in this context under the given key - or the given default value if undefined.
     */
    <T> T get(CompletionContextKey<T> key, @Nullable T defaultValue);

    /**
     * Stores a new value or a {@link ICompletionContextFunction} under the given key.
     */
    <T> void set(CompletionContextKey<T> key, T value);

    /**
     * Returns a snapshot view on all values currently defined in this context.
     */
    ImmutableMap<CompletionContextKey, Object> values();
}
