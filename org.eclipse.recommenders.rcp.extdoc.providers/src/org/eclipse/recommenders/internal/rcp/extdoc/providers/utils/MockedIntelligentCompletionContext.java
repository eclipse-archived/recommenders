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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import java.util.Set;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerAstCompletionNodeFinder;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

// TODO: Lots of methods taken from IntelligentCompletionContext.java
@SuppressWarnings("restriction")
public class MockedIntelligentCompletionContext implements IIntelligentCompletionContext {

    private final IJavaElementSelection selection;
    private final JavaElementResolver resolver;
    private CompilationUnit compilationUnit;
    private CompilerAstCompletionNodeFinder astCompletionNodeFinder;

    public MockedIntelligentCompletionContext(final IJavaElementSelection selection, final JavaElementResolver resolver) {
        this.selection = selection;
        this.resolver = resolver;
    }

    @Override
    public final String getPrefixToken() {
        throw new IllegalAccessError();
    }

    @Override
    public final Statement getCompletionNode() {
        // AnonymousMemberAccessVariableUsageResolver asks for it.
        return null;
    }

    @Override
    public final Statement getCompletionNodeParent() {
        throw new IllegalAccessError();
    }

    @Override
    public final Set<LocalDeclaration> getLocalDeclarations() {
        throw new IllegalAccessError();
    }

    @Override
    public final Set<FieldDeclaration> getFieldDeclarations() {
        throw new IllegalAccessError();
    }

    @Override
    public final Set<CompletionProposal> getJdtProposals() {
        throw new IllegalAccessError();
    }

    @Override
    public final ICompilationUnit getCompilationUnit() {
        if (compilationUnit == null && selection.getCompilationUnit() instanceof CompilationUnit) {
            compilationUnit = Checks.cast(selection.getCompilationUnit());
        }
        return compilationUnit;
    }

    @Override
    public IMethodName getEnclosingMethod() {
        final IMethod enclosingMethod = findEnclosingMethod();
        return enclosingMethod == null ? null : resolver.toRecMethod(enclosingMethod);
    }

    private IMethod findEnclosingMethod() {
        IJavaElement parent = selection.getJavaElement().getParent();
        while (parent != null && !(parent instanceof IMethod)) {
            parent = parent.getParent();
        }
        return parent == null ? null : (IMethod) parent;
    }

    @Override
    public final ITypeName getEnclosingType() {
        throw new IllegalAccessError();
    }

    @Override
    public final ITypeName getReceiverType() {
        throw new IllegalAccessError();
    }

    @Override
    public final String getReceiverName() {
        // AnonymousMemberAccessVariableUsageResolver asks for it.
        return null;
    }

    @Override
    public final boolean expectsReturnValue() {
        throw new IllegalAccessError();
    }

    @Override
    public final ITypeName getExpectedType() {
        throw new IllegalAccessError();
    }

    @Override
    public final Region getReplacementRegion() {
        throw new IllegalAccessError();
    }

    @Override
    public final JavaContentAssistInvocationContext getOriginalContext() {
        throw new IllegalAccessError();
    }

    @Override
    public Variable getVariable() {
        throw new IllegalAccessError();
    }

    @Override
    public final IJavaCompletionProposal toJavaCompletionProposal(final CompletionProposal proposal) {
        throw new IllegalAccessError();
    }

    @Override
    public final int getInvocationOffset() {
        return selection.getInvocationOffset();
    }

    @Override
    public IMethodName getEnclosingMethodsFirstDeclaration() {
        final IMethod enclosingMethod = findEnclosingMethod();
        if (enclosingMethod == null) {
            return null;
        }
        final IMethod method = Checks.cast(findEnclosingMethod().getPrimaryElement());
        try {
            final MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache.getMethodOverrideTester(method
                    .getDeclaringType());
            final IMethod declaringMethod = methodOverrideTester.findDeclaringMethod(method, true);
            return declaringMethod == null ? null : resolver.toRecMethod(declaringMethod);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final boolean isReceiverImplicitThis() {
        return ("".equals(getReceiverName()) || astCompletionNodeFinder.completionNode instanceof CompletionOnSingleNameReference)
                && getReceiverType() == null;
    }

    @Override
    public final Variable findMatchingVariable(final String variableName) {
        throw new IllegalAccessError();
    }

}