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
package org.eclipse.recommenders.rcp.codecompletion;

import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.annotations.Experimental;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;
import org.eclipse.recommenders.commons.utils.annotations.ReportUsage;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;

@Provisional
@SuppressWarnings("restriction")
public interface IIntelligentCompletionContext {

    /**
     * Returns the token preceding the location in front of the cursor position
     * where the code completion event occurred. This token includes every valid
     * Java identifier character until the first dot or space etc. occurs.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     *  c.prefix|&lt;ctrl-space&gt; // --&gt; "prefix"
     *  c.|&lt;ctrl-space&gt; // --&gt; ""
     * </pre>
     */
    public String getPrefixToken();

    /**
     * The (internal) AST node representing completion node created by the
     * completion completion parser.
     * 
     * @see one of org.eclipse.jdt.internal.codeassist.complete
     * @see CompletionOnMessageSend
     * @see CompletionOnQualifiedNameReference
     * @see CompletionOnSingleNameReference
     */
    public ASTNode getCompletionNode();

    /**
     * Returns a set of all local variables declared in the method code
     * completion was triggered in.
     * <p>
     * Note, the objects returned by this method are compiler ast nodes. This
     * API is very likely to change!
     * </p>
     */
    @Experimental
    Set<LocalDeclaration> getLocalDeclarations();

    /**
     * Returns a set of all fields declared in the scope of this compilation
     * unit( or just type?) completion was triggered in.
     * <p>
     * Note, the objects returned by this method are compiler ast nodes. This
     * API is very likely to change!
     * </p>
     */
    @Experimental
    Set<FieldDeclaration> getFieldDeclarations();

    /**
     * Returns proposals made by the JDT. These proposals are allowed to be
     * reused by completion engines. Changes to this set are not permitted.
     */
    public Set<CompletionProposal> getJdtProposals();

    /**
     * Returns the compilation unit the recommendations have been requested for.
     */
    public ICompilationUnit getCompilationUnit();

    /**
     * The innermost method that encloses the completion request.
     * <p>
     * Example:
     * 
     * <pre>
     *  public void aMethod(){
     *   ...
     *   b.|&lt;^Space&gt;
     * }
     *  -&gt; aMethod()
     * </pre>
     * 
     * <pre>
     * TODO How about anonymous inner types declared in methods? How does this look like?
     * </pre>
     */
    public IMethodName getEnclosingMethod();

    /**
     * The innermost type that encloses the completion request.
     */
    public ITypeName getEnclosingType();

    /**
     * Returns the static type of the receiver code completion was triggered on
     * - if it could be resolved from AST.
     * 
     */
    ITypeName getReceiverType();

    /**
     * Returns the name of the receiver this event was triggered on.
     * <p>
     * Example:
     * 
     * <pre>
     * container.|&lt;^Space&gt; =&gt;'container'
     * </pre>
     * 
     * But it may also contain a (unresolved) simple type name:
     * 
     * <pre>
     * PlatformUI|&lt;^Space&gt; =&gt;'PlatformUI'
     * </pre>
     */
    String getReceiverName();

    /**
     * Returns <code>true</code> if this completion event requests an expression
     * that returns 'something'. If 'something' is known, it is made available
     * under {@link #getExpectedType()}.
     * 
     * @return
     */
    public boolean expectsReturnValue();

    /**
     * Returns the type of the requested expression has to return - if known.
     * <p>
     * Example:
     * 
     * <pre>
     *   TODO pick one
     * </pre>
     * 
     */
    ITypeName getExpectedType();

    /**
     * The region of the text selection to replace. May be empty.
     */
    Region getReplacementRegion();

    /**
     * Returns the original Java completion context provided by Eclipse.
     * <p>
     * Please report reasons why you need the original context to the Code
     * Recommenders team since this completion context is intended to replace
     * the jdt context inside the Code Recommenders platform.
     */
    @ReportUsage
    JavaContentAssistInvocationContext getOriginalContext();

    /**
     * <b>NOTE:</b> This method returns a handle for a {@link Variable} which
     * might be used to lookup real variable definitions of a
     * {@link CompilationUnit}. Its methods will not return any receiver call
     * sites etc. Just the name, the type (if known) and the method that
     * declares this variable.
     */
    public Variable getVariable();

    /**
     * Utility method to convert JDT {@link CompletionProposal} internal
     * proposals to {@link IJavaCompletionProposal}s.
     */
    public IJavaCompletionProposal toJavaCompletionProposal(CompletionProposal proposal);

    public int getInvocationOffset();

    IMethodName getEnclosingMethodsFirstDeclaration();

}
