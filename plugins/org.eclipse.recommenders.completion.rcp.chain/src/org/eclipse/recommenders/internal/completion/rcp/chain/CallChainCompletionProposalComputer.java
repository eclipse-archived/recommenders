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

import static org.eclipse.recommenders.rcp.utils.JdtUtils.createUnresolvedField;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.createUnresolvedLocaVariable;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.createUnresolvedMethod;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.createUnresolvedType;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findTypeFromSignature;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findTypeOfField;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.completion.rcp.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.utils.CompilerBindings;
import org.eclipse.recommenders.rcp.utils.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class CallChainCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private IIntelligentCompletionContext recContext;
    private InternalCompletionContext jdtCoreContext;
    private IType expectedType;
    private List<MemberEdge> entrypoints;
    private List<ICompletionProposal> proposals;
    private final IntelligentCompletionContextResolver contextResolver;
    private JavaContentAssistInvocationContext jdtContext;

    @Inject
    public CallChainCompletionProposalComputer(final IntelligentCompletionContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
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
            executeCallChainSearch();
        } catch (final Exception e) {
            logError(e);
        }
        return proposals;
    }

    /**
     * @return true iff the context could be initialized successfully, i.e., completion context is a java context, and
     *         the core context is an extended context
     */
    private void initalizeContexts(final ContentAssistInvocationContext context) {
        if (context instanceof JavaContentAssistInvocationContext) {
            this.jdtContext = (JavaContentAssistInvocationContext) context;
            this.recContext = contextResolver.resolveContext(jdtContext);
            this.jdtCoreContext = recContext.getCoreCompletionContext();
        }
    }

    private boolean allRequiredContextsAvailable() {
        return recContext != null && jdtContext != null && jdtCoreContext != null && jdtCoreContext.isExtended();
    }

    private boolean findExpectedType() {
        expectedType = jdtContext.getExpectedType();
        return expectedType != null;
    }

    private boolean findEntrypoints() {
        entrypoints = new LinkedList<MemberEdge>();
        final ASTNode node = recContext.getCompletionNode();

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
            final IType type = createUnresolvedType((TypeBinding) b);
            addPublicStaticMembersToEntrypoints(type);
            break;
        case Binding.FIELD:
            final IField field = createUnresolvedField((FieldBinding) b);
            final Optional<IType> optType = findTypeOfField(field);
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
        final String token = recContext.getPrefixToken();
        final boolean prefixMatch = m.getElementName().startsWith(token);
        return prefixMatch;
    }

    private JavaElement findEnclosingElement() {
        final IJavaElement enclosing = jdtCoreContext.getEnclosingElement();
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
            final IType type = createUnresolvedType((TypeBinding) b);
            // note: not static!
            addPublicInstanceMembersToEntrypoints(type);
            break;
        case Binding.LOCAL:
            // TODO Review: could this be a field?
            final ILocalVariable var = createUnresolvedLocaVariable((VariableBinding) b, findEnclosingElement());
            addPublicInstanceMembersToEntrypoints(var);
        default:
            break;
        }
    }

    private void findEntrypointsForCompletionOnSingleName() {
        resolveEntrypoints(recContext.getFieldDeclarations());
        resolveEntrypoints(recContext.getLocalDeclarations());
        resolveEntrypoints(recContext.getMethodDeclarations());
        addAllInheritedPublicMembersToEntrypoints(findEnclosingType());
    }

    private void resolveEntrypoints(final Collection<? extends ASTNode> elements) {
        for (final ASTNode decl : elements) {
            final Optional<Binding> optBinding = CompilerBindings.getBinding(decl);
            if (!optBinding.isPresent()) {
                continue;
            }

            final Binding b = optBinding.get();
            IJavaElement javaElement = null;
            switch (b.kind()) {
            case Binding.TYPE:
                break;
            case Binding.METHOD:
                javaElement = createUnresolvedMethod((MethodBinding) b);
                break;
            case Binding.FIELD:
                javaElement = createUnresolvedField((FieldBinding) b);
                break;
            case Binding.LOCAL:
                javaElement = createUnresolvedLocaVariable((VariableBinding) b, findEnclosingElement());
                break;
            default:
                continue;
            }
            final MemberEdge e = new MemberEdge(javaElement);
            if (e.getReturnType().isPresent()) {
                entrypoints.add(e);
            }
        }
    }

    private void addAllInheritedPublicMembersToEntrypoints(final IType type) {
        final Collection<IMember> methodsAndFields = findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods(type);

        for (final IMember m : methodsAndFields) {
            if (m.getDeclaringType() != type) {
                entrypoints.add(new MemberEdge(m));
            }
        }
    }

    private IType findEnclosingType() {
        final IJavaElement enclosing = jdtCoreContext.getEnclosingElement();
        return (IType) enclosing.getAncestor(IJavaElement.TYPE).getPrimaryElement();
    }

    private void executeCallChainSearch() throws JavaModelException {
        proposals.clear();

        final GraphBuilder b = new GraphBuilder();
        b.build(entrypoints);
        final List<List<MemberEdge>> chains = b.findChains(expectedType);
        for (final List<MemberEdge> chain : chains) {
            final TemplateProposal completion = new CompletionTemplateBuilder().create(chain, jdtContext);
            final CallChainCompletionProposal completionProposal = new CallChainCompletionProposal(completion, chain);
            proposals.add(completionProposal);
        }
    }

    private void logError(final Exception e) {
        RecommendersUtilsPlugin.logError(e, "Chain completion failed in %s.", jdtContext.getCompilationUnit()
                .getElementName());
    }

    @Override
    public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public void sessionStarted() {
        proposals = new LinkedList<ICompletionProposal>();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {

    }
}
