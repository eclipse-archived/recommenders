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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

@SuppressWarnings("restriction")
public class DelegatingIntelligentCompletionContext implements IIntelligentCompletionContext {

    public IIntelligentCompletionContext delegate;

    public DelegatingIntelligentCompletionContext(final IIntelligentCompletionContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public final String getPrefixToken() {
        return delegate.getPrefixToken();
    }

    @Override
    public final Statement getCompletionNode() {
        return delegate.getCompletionNode();
    }

    @Override
    public final Statement getCompletionNodeParent() {
        return delegate.getCompletionNodeParent();
    }

    @Override
    public final Set<LocalDeclaration> getLocalDeclarations() {
        return delegate.getLocalDeclarations();
    }

    @Override
    public final Set<FieldDeclaration> getFieldDeclarations() {
        return delegate.getFieldDeclarations();
    }

    @Override
    public final Set<CompletionProposal> getJdtProposals() {
        return delegate.getJdtProposals();
    }

    @Override
    public final ICompilationUnit getCompilationUnit() {
        return delegate.getCompilationUnit();
    }

    @Override
    public IMethodName getEnclosingMethod() {
        return delegate.getEnclosingMethod();
    }

    @Override
    public final ITypeName getEnclosingType() {
        return delegate.getEnclosingType();
    }

    @Override
    public final ITypeName getReceiverType() {
        return delegate.getReceiverType();
    }

    @Override
    public final String getReceiverName() {
        return delegate.getReceiverName();
    }

    @Override
    public final boolean expectsReturnValue() {
        return delegate.expectsReturnValue();
    }

    @Override
    public final ITypeName getExpectedType() {
        return delegate.getExpectedType();
    }

    @Override
    public final Region getReplacementRegion() {
        return delegate.getReplacementRegion();
    }

    @Override
    public final JavaContentAssistInvocationContext getOriginalContext() {
        return delegate.getOriginalContext();
    }

    @Override
    public Variable getVariable() {
        return delegate.getVariable();
    }

    @Override
    public final IJavaCompletionProposal toJavaCompletionProposal(final CompletionProposal proposal) {
        return delegate.toJavaCompletionProposal(proposal);
    }

    @Override
    public final int getInvocationOffset() {
        return delegate.getInvocationOffset();
    }

    @Override
    public IMethodName getEnclosingMethodsFirstDeclaration() {
        return delegate.getEnclosingMethodsFirstDeclaration();
    }

    @Override
    public final boolean isReceiverImplicitThis() {
        return delegate.isReceiverImplicitThis();
    }

    @Override
    public final Variable findMatchingVariable(final String variableName) {
        return delegate.findMatchingVariable(variableName);
    }

}