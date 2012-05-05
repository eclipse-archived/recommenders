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
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.recommenders.utils.rcp.JdtUtils.createUnresolvedField;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.createUnresolvedLocaVariable;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.createUnresolvedType;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findTypeFromSignature;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findTypeOfField;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class ChainCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private final Set<String> excludedTypes = new HashSet<String>() {
        {
            add("java.lang.Object");
            add("java.lang.String");
        }

    };
    private IRecommendersCompletionContext ctx;
    private IType expectedType;
    private List<MemberEdge> entrypoints;
    private String error;
    private final IRecommendersCompletionContextFactory ctxFactory;

    @Inject
    public ChainCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory) {
        this.ctxFactory = ctxFactory;
    }

    public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {

        initalizeContexts(context);
        if (!allRequiredContextsAvailable()) {
            return Collections.emptyList();
        }
        if (!findExpectedType()) {
            return Collections.emptyList();
        }
        if (!findEntrypoints()) {
            return Collections.emptyList();
        }
        try {
            return executeCallChainSearch();
        } catch (final Exception e) {
            logError(e);
            return Collections.emptyList();
        }
    }

    /**
     * @return true if the context could be initialized successfully, i.e., completion context is a java context, and
     *         the core context is an extended context
     */
    private void initalizeContexts(final ContentAssistInvocationContext context) {
        ctx = ctxFactory.create((JavaContentAssistInvocationContext) context);
    }

    private boolean allRequiredContextsAvailable() {
        return ctx != null;
    }

    private boolean findExpectedType() {
        expectedType = ctx.getExpectedType().orNull();
        if (expectedType == null) {
            return false;
        }
        if (excludedTypes.contains(expectedType.getFullyQualifiedName())) {
            expectedType = null;
            return false;
        }
        return true;
    }

    private boolean findEntrypoints() {
        entrypoints = new LinkedList<MemberEdge>();
        final ASTNode node = ctx.getCompletionNode();

        if (node instanceof CompletionOnQualifiedNameReference) {
            findEntrypointsForCompletionOnQualifiedName((CompletionOnQualifiedNameReference) node);
        } else if (node instanceof CompletionOnMemberAccess) {
            findEntrypointsForCompletionOnMemberAccess((CompletionOnMemberAccess) node);
        } else if (node instanceof CompletionOnSingleNameReference) {
            findEntrypointsForCompletionOnSingleName();
        }
        return !entrypoints.isEmpty();
    }

    private void findEntrypointsForCompletionOnQualifiedName(final CompletionOnQualifiedNameReference node) {
        final Binding b = node.binding;
        if (b == null) {
            return;
        }
        switch (b.kind()) {
        case Binding.TYPE:
            final Optional<IType> type = createUnresolvedType((TypeBinding) b);
            if (type.isPresent()) {
                addPublicStaticMembersToEntrypoints(type.get());
            }
            break;
        case Binding.FIELD:
            final Optional<IField> field = createUnresolvedField((FieldBinding) b);
            if (!field.isPresent()) {
                break;
            }
            final Optional<IType> optType = findTypeOfField(field.get());
            if (optType.isPresent()) {
                addPublicInstanceMembersToEntrypoints(optType.get());
            }
            break;
        case Binding.LOCAL:
            final ILocalVariable var = createUnresolvedLocaVariable((VariableBinding) b, findEnclosingElement());
            addPublicInstanceMembersToEntrypoints(var);
            break;
        }
    }

    private void addPublicStaticMembersToEntrypoints(final IType type) {
        for (final IMember m : findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods(type)) {
            if (passesPrefixCheck(m)) {
                final MemberEdge edge = new MemberEdge(m);
                entrypoints.add(edge);
            }
        }
    }

    private boolean passesPrefixCheck(final IMember m) {
        final String token = ctx.getPrefix();
        final boolean prefixMatch = m.getElementName().startsWith(token);
        return prefixMatch;
    }

    private JavaElement findEnclosingElement() {
        // should be save to call .get() here:
        final IJavaElement enclosing = ctx.getEnclosingElement().get();
        return (JavaElement) enclosing;
    }

    private void addPublicInstanceMembersToEntrypoints(final ILocalVariable var) {
        final Optional<IType> optType = findTypeFromSignature(var.getTypeSignature(), var);
        if (!optType.isPresent()) {
            return;
        }
        addPublicInstanceMembersToEntrypoints(optType.get());
    }

    private void addPublicInstanceMembersToEntrypoints(final IType type) {
        for (final IMember m : findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods(type)) {
            if (passesPrefixCheck(m)) {
                final MemberEdge edge = new MemberEdge(m);
                entrypoints.add(edge);
            }
        }
    }

    private void findEntrypointsForCompletionOnMemberAccess(final CompletionOnMemberAccess node) {
        final Binding b = node.actualReceiverType;
        if (b == null) {
            return;
        }
        switch (b.kind()) {
        case Binding.TYPE:
            final Optional<IType> type = createUnresolvedType((TypeBinding) b);
            // note: not static!
            if (type.isPresent()) {
                addPublicInstanceMembersToEntrypoints(type.get());
            }
            break;
        case Binding.LOCAL:
            // TODO Review: could this be a field?
            final ILocalVariable var = createUnresolvedLocaVariable((VariableBinding) b, findEnclosingElement());
            addPublicInstanceMembersToEntrypoints(var);
            break;
        default:
            break;
        }
    }

    private void findEntrypointsForCompletionOnSingleName() {
        resolveEntrypoints(ctx.getVisibleFields());
        resolveEntrypoints(ctx.getVisibleLocals());
        resolveEntrypoints(ctx.getVisibleMethods());
    }

    private void resolveEntrypoints(final Collection<? extends IJavaElement> elements) {
        for (final IJavaElement decl : elements) {
            if (!matchesPrefixToken(decl)) {
                continue;
            }
            final MemberEdge e = new MemberEdge(decl);
            if (e.getReturnType().isPresent()) {
                entrypoints.add(e);
            }
        }
    }

    private boolean matchesPrefixToken(final IJavaElement decl) {
        final String prefix = ctx.getPrefix();
        final String elementName = decl.getElementName();
        return elementName.startsWith(prefix);
    }

    private List<ICompletionProposal> executeCallChainSearch() throws JavaModelException {
        final GraphBuilder b = new GraphBuilder();
        final int expectedDimension = Signature.getArrayCount(ctx.getExpectedTypeSignature().get().toCharArray());
        try {
            new SimpleTimeLimiter().callWithTimeout(new Callable<Void>() {

                public Void call() throws Exception {
                    b.startChainSearch(findEnclosingElement(), entrypoints, expectedType, expectedDimension);
                    return null;
                }
            }, 3500, TimeUnit.MILLISECONDS, true);
        } catch (final Exception e) {
            setError("Timeout limit hit during call chain computation.");
        }
        final List<ICompletionProposal> proposals = Lists.newLinkedList();
        for (final List<MemberEdge> chain : b.getChains()) {
            final TemplateProposal completion = new CompletionTemplateBuilder().create(chain, expectedDimension,
                    ctx.getJavaContext());
            final ChainCompletionProposal completionProposal = new ChainCompletionProposal(completion, chain);
            proposals.add(completionProposal);
        }
        return proposals;
    }

    private void setError(final String errorMessage) {
        error = errorMessage;
    }

    private void logError(final Exception e) {
        RecommendersUtilsPlugin
                .logError(e, "Chain completion failed in %s.", ctx.getCompilationUnit().getElementName());
    }

    public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    public void sessionStarted() {
        setError(null);
    }

    public String getErrorMessage() {
        return error;
    }

    public void sessionEnded() {

    }
}
