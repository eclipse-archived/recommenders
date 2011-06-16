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

public class DelegatingIntelligentCompletionContext implements IIntelligentCompletionContext {
    public IIntelligentCompletionContext delegate;

    public DelegatingIntelligentCompletionContext(final IIntelligentCompletionContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getPrefixToken() {
        return delegate.getPrefixToken();
    }

    @Override
    public Statement getCompletionNode() {
        return delegate.getCompletionNode();
    }

    @Override
    public Statement getCompletionNodeParent() {
        return delegate.getCompletionNodeParent();
    }

    @Override
    public Set<LocalDeclaration> getLocalDeclarations() {
        return delegate.getLocalDeclarations();
    }

    @Override
    public Set<FieldDeclaration> getFieldDeclarations() {
        return delegate.getFieldDeclarations();
    }

    @Override
    public Set<CompletionProposal> getJdtProposals() {
        return delegate.getJdtProposals();
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
        return delegate.getCompilationUnit();
    }

    @Override
    public IMethodName getEnclosingMethod() {
        return delegate.getEnclosingMethod();
    }

    @Override
    public ITypeName getEnclosingType() {
        return delegate.getEnclosingType();
    }

    @Override
    public ITypeName getReceiverType() {
        return delegate.getReceiverType();
    }

    @Override
    public String getReceiverName() {
        return delegate.getReceiverName();
    }

    @Override
    public boolean expectsReturnValue() {
        return delegate.expectsReturnValue();
    }

    @Override
    public ITypeName getExpectedType() {
        return delegate.getExpectedType();
    }

    @Override
    public Region getReplacementRegion() {
        return delegate.getReplacementRegion();
    }

    @Override
    public JavaContentAssistInvocationContext getOriginalContext() {
        return delegate.getOriginalContext();
    }

    @Override
    public Variable getVariable() {
        return delegate.getVariable();
    }

    @Override
    public IJavaCompletionProposal toJavaCompletionProposal(final CompletionProposal proposal) {
        return delegate.toJavaCompletionProposal(proposal);
    }

    @Override
    public int getInvocationOffset() {
        return delegate.getInvocationOffset();
    }

    @Override
    public IMethodName getEnclosingMethodsFirstDeclaration() {
        return delegate.getEnclosingMethodsFirstDeclaration();
    }

    @Override
    public boolean isReceiverImplicitThis() {
        return delegate.isReceiverImplicitThis();
    }

    @Override
    public Variable findMatchingVariable(final String variableName) {
        return delegate.findMatchingVariable(variableName);
    }

}