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
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class RecommendersCompletionContext extends BaseRecommendersCompletionContext {

    private CompilerAstCompletionNodeFinder astCompletionNodeFinder;

    private org.eclipse.jdt.internal.core.CompilationUnit jdtCompilationUnit;

    private CompletionEngine completionEngine;

    private CompletionParser completionParser;

    private IntelligentCompletionRequestor completionRequestor;

    @Inject
    public RecommendersCompletionContext(@Assisted final JavaContentAssistInvocationContext jdtContext,
            final IAstProvider astProvider) {
        super(jdtContext, astProvider);
        jdtContext.getExpectedType();
        jdtCompilationUnit = (org.eclipse.jdt.internal.core.CompilationUnit) getJavaContext().getCompilationUnit();
        // initializeCompletionPrefixToken();
        initializeRequestor();
        initializeCompletionEngine();
        performCodeCompletion();
        findCompletionNode();
        clearStateIfNoCompletionNodeFound();
    }

    // private void initializeCompletionPrefixToken() {
    // try {
    // token = getJavaContext().computeIdentifierPrefix().toString();
    // } catch (final BadLocationException x) {
    // RecommendersUtilsPlugin.logError(x, "Computing token from active editor failed.");
    // token = "";
    // }
    // }

    private void initializeRequestor() {
        completionRequestor = new IntelligentCompletionRequestor(jdtCompilationUnit);
    }

    private void initializeCompletionEngine() {
        try {
            final JavaProject project = (JavaProject) jdtCompilationUnit.getJavaProject();
            final WorkingCopyOwner owner = jdtCompilationUnit.getOwner();
            final SearchableEnvironment s = project.newSearchableNameEnvironment(owner);
            completionEngine = new CompletionEngine(s, completionRequestor, project.getOptions(true), project, owner,
                    new NullProgressMonitor());
        } catch (final JavaModelException x) {
            throwUnhandledException(x);
        }
    }

    private void performCodeCompletion() {
        final org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu;
        if (jdtCompilationUnit.isWorkingCopy()) {
            compilerCu = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) jdtCompilationUnit
                    .getOriginalElement();
        } else {
            compilerCu = jdtCompilationUnit;
        }
        completionEngine.complete(compilerCu, getJavaContext().getInvocationOffset(), 0,
                jdtCompilationUnit.getPrimary());
        completionParser = (CompletionParser) completionEngine.getParser();
    }

    private void findCompletionNode() {
        astCompletionNodeFinder = new CompilerAstCompletionNodeFinder();
        final ReferenceContext referenceContext = completionParser.referenceContext;
        if (completionParser.compilationUnit != null) {
            final CompilationUnitDeclaration compilationUnit = completionParser.compilationUnit;
            // completion parser sets this to true after his run and thus
            // prevents visitors to visit this cu a second time. Reset this
            // state:
            compilationUnit.ignoreFurtherInvestigation = false;
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        } else if (referenceContext instanceof CompilationUnitDeclaration) {
            final CompilationUnitDeclaration compilationUnit = cast(referenceContext);
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        } else if (referenceContext instanceof AbstractMethodDeclaration) {
            final CompilationUnitDeclaration compilationUnit = findCompilationUnit((AbstractMethodDeclaration) referenceContext);
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        }
    }

    private CompilationUnitDeclaration findCompilationUnit(final AbstractMethodDeclaration methodContext) {
        Scope tmp = methodContext.scope;
        while (tmp != null) {
            tmp = tmp.parent;
            if (tmp instanceof CompilationUnitScope) {
                final CompilationUnitScope scope = cast(tmp);
                return scope.referenceContext;
            }
        }
        return null;
    }

    private void clearStateIfNoCompletionNodeFound() {
        if (!astCompletionNodeFinder.isCompletionNodeFound()) {
            astCompletionNodeFinder.clearState();
            jdtCompilationUnit = null;
            completionEngine = null;
            completionParser = null;
        }
    }

    @Override
    public Optional<ASTNode> getCompletionNode() {
        ASTNode node = astCompletionNodeFinder.completionNode;
        return Optional.fromNullable(node);
    }

    @Override
    public Optional<ASTNode> getCompletionNodeParent() {
        ASTNode node = astCompletionNodeFinder.completionNodeParent();
        return Optional.fromNullable(node);
    }

    @Override
    public List<IField> getVisibleFields() {
        // TODO this does not consider methods declared in the super type
        final List<IField> res = Lists.newArrayList();
        for (final FieldDeclaration d : astCompletionNodeFinder.fieldDeclarations) {
            final FieldBinding b = d.binding;
            final Optional<IField> opt = JdtUtils.createUnresolvedField(b);
            if (opt.isPresent()) {
                res.add(opt.get());
            }
        }
        addInheritedMembers(res, IJavaElement.FIELD);
        return res;
    }

    private void addInheritedMembers(final List res, final int javaElementType) {
        final Optional<IType> opt = getClosestEnclosingType();
        if (opt.isPresent()) {
            // instance:
            for (final IMember m : JdtUtils.findAllPublicInstanceFieldsAndPublicInstanceMethods(opt.get())) {
                if (javaElementType == m.getElementType()) {
                    res.add(m);
                }
            }
        }
        // static:
        for (final IMember m : JdtUtils.findAllPublicStaticFieldsAndStaticMethods(opt.get())) {
            if (javaElementType == m.getElementType()) {
                res.add(m);
            }
        }
    }

    @Override
    public List<ILocalVariable> getVisibleLocals() {
        if (!hasEnclosingElement()) {
            return Collections.emptyList();
        }
        final List<ILocalVariable> res = Lists.newArrayList();
        for (final LocalDeclaration d : astCompletionNodeFinder.localDeclarations) {
            final LocalVariableBinding b = d.binding;
            final JavaElement parent = (JavaElement) getEnclosingElement().get();
            final ILocalVariable f = JdtUtils.createUnresolvedLocaVariable(b, parent);
            res.add(f);
        }
        return res;
    }

    @Override
    public List<IMethod> getVisibleMethods() {
        // TODO this does not consider methods declared in the super type
        final List<IMethod> res = Lists.newArrayList();
        for (final MethodDeclaration d : astCompletionNodeFinder.methodDeclarations) {
            final MethodBinding b = d.binding;
            if (b != null) {
                final Optional<IMethod> opt = JdtUtils.createUnresolvedMethod(b);
                if (opt.isPresent()) {
                    res.add(opt.get());
                }
            }
        }
        addInheritedMembers(res, IJavaElement.METHOD);
        return res;
    }
}
