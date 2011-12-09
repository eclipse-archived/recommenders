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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.utils;

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.completion.rcp.CompilerAstCompletionNodeFinder;
import org.eclipse.recommenders.internal.completion.rcp.CompilerBindings;
import org.eclipse.recommenders.internal.completion.rcp.IntelligentCompletionRequestor;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

// TODO: Lots of methods taken from IntelligentCompletionContext.java
@SuppressWarnings("restriction")
public class MockedIntelligentCompletionContext implements IIntelligentCompletionContext {

    private final IJavaElementSelection selection;
    private CompilationUnit compilationUnit;

    private CompilerAstCompletionNodeFinder astCompletionNodeFinder;
    private IntelligentCompletionRequestor completionRequestor;
    private CompletionEngine completionEngine;
    private CompletionParser completionParser;

    public MockedIntelligentCompletionContext(final IJavaElementSelection selection) {
        this.selection = selection;
    }

    private CompilerAstCompletionNodeFinder getNodeFinder() {
        if (astCompletionNodeFinder == null) {
            astCompletionNodeFinder = new CompilerAstCompletionNodeFinder();
            performCodeCompletion();
            final ReferenceContext referenceContext = completionParser.referenceContext;
            if (completionParser.compilationUnit != null) {
                final CompilationUnitDeclaration compUnit = completionParser.compilationUnit;
                // completion parser sets this to true after his run and thus
                // prevents visitors to visit this cu a second time. Reset this
                // state:
                compUnit.ignoreFurtherInvestigation = false;
                compUnit.traverse(astCompletionNodeFinder, compUnit.scope);
            } else if (referenceContext instanceof CompilationUnitDeclaration) {
                final CompilationUnitDeclaration compUnit = Checks.cast(referenceContext);
                compUnit.traverse(astCompletionNodeFinder, compUnit.scope);
            } else if (referenceContext instanceof AbstractMethodDeclaration) {
                final CompilationUnitDeclaration compUnit = findCompilationUnit((AbstractMethodDeclaration) referenceContext);
                compUnit.traverse(astCompletionNodeFinder, compUnit.scope);
            }
        }
        return astCompletionNodeFinder;
    }

    private void performCodeCompletion() {
        initializeCompletionEngine();
        org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu = (CompilationUnit) getCompilationUnit();
        if (getCompilationUnit().isWorkingCopy()) {
            compilerCu = Checks.cast(getCompilationUnit().getOriginalElement());
        }
        completionEngine.complete(compilerCu, getInvocationOffset(), 0, getCompilationUnit().getPrimary());
        completionParser = Checks.cast(completionEngine.getParser());
    }

    private void initializeCompletionEngine() {
        try {
            final JavaProject project = (JavaProject) getCompilationUnit().getJavaProject();
            final WorkingCopyOwner owner = getCompilationUnit().getOwner();
            final SearchableEnvironment s = project.newSearchableNameEnvironment(owner);
            completionRequestor = new IntelligentCompletionRequestor((CompilationUnit) getCompilationUnit());
            completionEngine = new CompletionEngine(s, completionRequestor, project.getOptions(true), project, owner,
                    new NullProgressMonitor());
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private static CompilationUnitDeclaration findCompilationUnit(final AbstractMethodDeclaration methodContext) {
        Scope tmp = methodContext.scope;
        while (tmp != null) {
            tmp = tmp.parent;
            if (tmp instanceof CompilationUnitScope) {
                final CompilationUnitScope scope = Checks.cast(tmp);
                return scope.referenceContext;
            }
        }
        return null;
    }

    @Override
    public final String getPrefixToken() {
        return "";
        // throw new IllegalAccessError();
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
        return getNodeFinder().fieldDeclarations;
    }

    @Override
    public Set<MethodDeclaration> getMethodDeclarations() {
        return getNodeFinder().methodDeclarations;
    }

    @Override
    public InternalCompletionContext getCoreCompletionContext() {
        return completionRequestor.getCompletionContext();
    }

    @Override
    public final Set<CompletionProposal> getJdtProposals() {
        throw new IllegalAccessError();
    }

    @Override
    public final ICompilationUnit getCompilationUnit() {
        if (compilationUnit == null) {
            final ITypeRoot unit = Checks.ensureIsNotNull(selection.getCompilationUnit());
            if (unit instanceof ClassFile) {
                compilationUnit = Checks.cast(((ClassFile) unit).getCompilationUnit());
            } else {
                compilationUnit = Checks.cast(unit);
            }
            Checks.ensureIsNotNull(compilationUnit);
        }
        return compilationUnit;
    }

    @Override
    public IMethodName getEnclosingMethod() {
        final IMethod enclosingMethod = findEnclosingMethod();
        return enclosingMethod == null ? null : ElementResolver.toRecMethod(enclosingMethod);
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
        return CompilerBindings.toTypeName(getNodeFinder().receiverType).orNull();
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
            return declaringMethod == null ? null : ElementResolver.toRecMethod(declaringMethod);
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final boolean isReceiverImplicitThis() {
        return (getReceiverName().isEmpty() || getNodeFinder().completionNode instanceof CompletionOnSingleNameReference)
                && getReceiverType() == null;
    }

    @Override
    public final Variable findMatchingVariable(final String variableName) {
        throw new IllegalAccessError();
    }

}