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
package org.eclipse.recommenders.completion.rcp.chain.jdt;

import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.createUnresolvedField;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.createUnresolvedLocaVariable;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.createUnresolvedMethod;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.createUnresolvedType;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.findAllPublicStaticFieldsAndNonVoidNonPrimitiveMethods;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Optional;

@SuppressWarnings("restriction")
public class CallChainCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private JavaContentAssistInvocationContext context;
    private InternalCompletionContext internalContext;
    private IType expectedType;
    private List<CallChainEdge> entrypoints;
    private List<ICompletionProposal> proposals;

    public CallChainCompletionProposalComputer() {
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {

        if (!initalizeContext(context)) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return proposals;
    }

    /**
     * @return true iff the context could be initialized successfully, i.e., completion context is a java context, and
     *         the core context is an extended context
     */
    private boolean initalizeContext(final ContentAssistInvocationContext completionContext) {
        if (completionContext instanceof JavaContentAssistInvocationContext) {
            this.context = (JavaContentAssistInvocationContext) completionContext;
            internalContext = (InternalCompletionContext) context.getCoreContext();
        }
        return context != null && internalContext.isExtended();
    }

    private boolean findExpectedType() {
        expectedType = context.getExpectedType();
        return expectedType != null;
    }

    private boolean findEntrypoints() {
        entrypoints = new LinkedList<CallChainEdge>();
        final ASTNode completionNode = internalContext.getCompletionNode();

        if (completionNode instanceof CompletionOnQualifiedNameReference) {
            final CompletionOnQualifiedNameReference c = (CompletionOnQualifiedNameReference) completionNode;
            final Binding binding = c.binding;
            if (binding == null) {
                return false;
            }
            switch (binding.kind()) {
            case Binding.TYPE:
                final IType type = createUnresolvedType((TypeBinding) binding);
                addPublicStaticMembersToEntrypoints(type);
                break;
            case Binding.LOCAL:
                final ILocalVariable var = createUnresolvedLocaVariable((VariableBinding) binding,
                        (JavaElement) internalContext.getEnclosingElement());
                addPublicMembersToEntrypoints(var);
            default:
                break;
            }
        } else if (completionNode instanceof CompletionOnMemberAccess) {
            final Binding binding = ((CompletionOnMemberAccess) completionNode).actualReceiverType;
            if (binding == null) {
                return false;
            }
            switch (binding.kind()) {
            case Binding.TYPE:
                final IType type = createUnresolvedType((TypeBinding) binding);
                addPublicMembersToEntrypoints(type);
                break;
            case Binding.LOCAL:
                final ILocalVariable var = createUnresolvedLocaVariable((VariableBinding) binding,
                        (JavaElement) internalContext.getEnclosingElement());
                addPublicMembersToEntrypoints(var);
            default:
                break;
            }
        } else {

            resolveEntrypoints(internalContext.getVisibleFields());
            resolveEntrypoints(internalContext.getVisibleMethods());
            resolveEntrypoints(internalContext.getVisibleLocalVariables());
        }
        return !entrypoints.isEmpty();
    }

    private void addPublicStaticMembersToEntrypoints(final IType type) {
        for (final IMember m : findAllPublicStaticFieldsAndNonVoidNonPrimitiveMethods(type)) {
            if (passesPrefixCheck(m)) {
                final CallChainEdge edge = new CallChainEdge(m);
                entrypoints.add(edge);
            }
        }
    }

    private void addPublicMembersToEntrypoints(final ILocalVariable var) {
        final Optional<IType> oType = InternalAPIsHelper.findTypeFromSignature(var.getTypeSignature(), var);
        if (!oType.isPresent()) {
            return;
        }
        addPublicMembersToEntrypoints(oType.get());
    }

    private void addPublicMembersToEntrypoints(final IType type) {
        for (final IMember m : findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods(type)) {
            if (passesPrefixCheck(m)) {
                final CallChainEdge edge = new CallChainEdge(m);
                entrypoints.add(edge);
            }
        }
    }

    private boolean passesPrefixCheck(final IMember m) {
        final char[] token = context.getCoreContext().getToken();
        final boolean prefixMatch = m.getElementName().startsWith(new String(token));
        return prefixMatch;
    }

    private void resolveEntrypoints(final ObjectVector elements) {
        for (int i = elements.size(); i-- > 0;) {
            final Binding binding = (Binding) elements.elementAt(i);
            IJavaElement javaElement = null;
            switch (binding.kind()) {
            case Binding.TYPE:
                break;
            case Binding.METHOD:
                javaElement = createUnresolvedMethod((MethodBinding) binding);
                break;
            case Binding.FIELD:
                javaElement = createUnresolvedField((FieldBinding) binding);
                break;
            case Binding.LOCAL:
                javaElement = createUnresolvedLocaVariable((VariableBinding) binding,
                        (JavaElement) internalContext.getEnclosingElement());
                break;
            default:
                continue;
            }
            final CallChainEdge e = new CallChainEdge(javaElement);
            if (e.getReturnType().isPresent()) {
                entrypoints.add(e);
            }
        }
    }

    private void executeCallChainSearch() throws JavaModelException {
        proposals.clear();

        final CallChainGraphBuilder b = new CallChainGraphBuilder();
        b.build(entrypoints);
        final List<List<CallChainEdge>> chains = b.findChains(expectedType);
        for (final List<CallChainEdge> chain : chains) {
            final TemplateProposal completion = new CallChainCompletionTemplateBuilder().create(chain, context);
            final CallChainCompletionProposal completionProposal = new CallChainCompletionProposal(completion, chain);
            proposals.add(completionProposal);
        }
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
