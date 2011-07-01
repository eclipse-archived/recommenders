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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerAstCompletionNodeFinder;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerBindings;
import org.eclipse.recommenders.internal.rcp.codecompletion.IntelligentCompletionRequestor;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

// TODO: Lots of methods taken from IntelligentCompletionContext.java
@SuppressWarnings("restriction")
public class MockedIntelligentCompletionContext implements IIntelligentCompletionContext {

    private final IJavaElementSelection selection;
    private final JavaElementResolver resolver;
    private CompilationUnit compilationUnit;
    private CompilerAstCompletionNodeFinder astCompletionNodeFinder;

    private IntelligentCompletionRequestor completionRequestor;
    private CompletionEngine completionEngine;
    private CompletionParser completionParser;

    public MockedIntelligentCompletionContext(final IJavaElementSelection selection, final JavaElementResolver resolver) {
        this.selection = selection;
        this.resolver = resolver;
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
        org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu = compilationUnit;
        if (compilationUnit.isWorkingCopy()) {
            compilerCu = Checks.cast(compilationUnit.getOriginalElement());
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

    private CompilationUnitDeclaration findCompilationUnit(final AbstractMethodDeclaration methodContext) {
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
        throw new IllegalAccessError();
    }

    @Override
    public final Statement getCompletionNode() {
        return getNodeFinder().completionNode;
    }

    @Override
    public final Statement getCompletionNodeParent() {
        throw new IllegalAccessError();
    }

    @Override
    public final Set<LocalDeclaration> getLocalDeclarations() {
        return astCompletionNodeFinder.localDeclarations;
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
        if (compilationUnit == null) {
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
        return getNodeFinder().receiverName;
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
        if (isReceiverImplicitThis() || "this".equals(getReceiverName())) {
            return Variable.create("this", getSuperclassOfEnclosingType(), getEnclosingMethod());
        }

        if (getReceiverName() != null && getReceiverType() != null) {
            return Variable.create(getReceiverName(), getReceiverType(), getEnclosingMethod());
        }
        final LocalDeclaration match = findMatchingLocalVariable(getReceiverName());
        if (match == null) {
            return null;
        }
        final String name = String.valueOf(match.name);
        final ITypeName type = CompilerBindings.toTypeName(match.type);
        return Variable.create(name, type, getEnclosingMethod());
    }

    private ITypeName getSuperclassOfEnclosingType() {
        try {
            IType enclosingType = findEnclosingType();
            if (enclosingType == null) {
                return null;
            }
            // TODO :: Rework code to resolve supertype name... this is
            // odd/wrong location for this, right?
            enclosingType = JdtUtils.resolveJavaElementProxy(enclosingType);
            final ITypeHierarchy typeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(enclosingType);
            final IType superclass = typeHierarchy.getSuperclass(enclosingType);
            return superclass == null ? null : resolver.toRecType(superclass);
        } catch (final JavaModelException e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    private IType findEnclosingType() {
        final IJavaElement element = selection.getJavaElement().getParent();
        if (element instanceof IMethod) {
            return ((IMethod) element).getDeclaringType();
        } else if (element instanceof IField) {
            return ((IField) element).getDeclaringType();
        } else if (element instanceof IType) {
            return (IType) element;
        }
        return null;
    }

    private LocalDeclaration findMatchingLocalVariable(final String receiverName) {
        for (final LocalDeclaration local : getLocalDeclarations()) {
            final String name = String.valueOf(local.name);
            if (name.equals(receiverName)) {
                return local;
            }
        }
        return null;
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