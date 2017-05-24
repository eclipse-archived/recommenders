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
package org.eclipse.recommenders.internal.chain.rcp;

import static org.eclipse.recommenders.internal.chain.rcp.TypeBindingAnalyzer.*;
import static org.eclipse.recommenders.internal.chain.rcp.l10n.LogMessages.WARNING_CANNOT_HANDLE_FOR_FINDING_ENTRY_POINTS;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedAllocationExpression;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.chain.rcp.ChainRcpModule.ChainCompletion;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SimpleTimeLimiter;

@SuppressWarnings("restriction")
public class ChainCompletionProposalComputer implements IJavaCompletionProposalComputer {

    public static final String CATEGORY_ID = "org.eclipse.recommenders.chain.rcp.proposalCategory.chain"; //$NON-NLS-1$

    private IRecommendersCompletionContext ctx;
    private List<ChainElement> entrypoints;
    private String error;
    private final IPreferenceStore prefStore;
    private Scope scope;
    private InvocationSite invocationSite;
    private IAstProvider astProvider;

    @Inject
    public ChainCompletionProposalComputer(IAstProvider astProvider,
            @ChainCompletion final IPreferenceStore preferenceStore) {
        this.astProvider = astProvider;
        prefStore = preferenceStore;
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        if (!shouldMakeProposals()) {
            return Collections.emptyList();
        }
        if (!initializeRequiredContext(context)) {
            return Collections.emptyList();
        }
        if (!shouldPerformCompletionOnExpectedType()) {
            return Collections.emptyList();
        }
        if (!findEntrypoints()) {
            return Collections.emptyList();
        }
        return executeCallChainSearch();
    }

    @VisibleForTesting
    /**
     * Ensures that we only make recommendations if we are not on the default tab. Disables this engine if the user has
     * activated chain completion on default content assist list
     */
    protected boolean shouldMakeProposals() {
        final Set<String> excluded = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        if (excluded.contains(CATEGORY_ID)) {
            // we are excluded on default tab? Then we are not on default tab NOW. We are on a subsequent tab and should
            // make completions:
            return true;
        }
        // disable and stop computing.
        new DisableContentAssistCategoryJob(CATEGORY_ID).schedule(300);
        return false;
    }

    private boolean initializeRequiredContext(final ContentAssistInvocationContext context) {
        if (!(context instanceof JavaContentAssistInvocationContext)) {
            return false;
        }
        JavaContentAssistInvocationContext jdtCtx = (JavaContentAssistInvocationContext) context;

        ctx = new RecommendersCompletionContext(jdtCtx, astProvider);
        final Optional<Scope> optionalScope = ScopeAccessWorkaround.resolveScope(ctx);
        if (!optionalScope.isPresent()) {
            return false;
        }
        scope = optionalScope.get();
        return true;
    }

    private boolean shouldPerformCompletionOnExpectedType() {
        final Optional<IType> expected = ctx.getExpectedType();
        return expected.isPresent() || !ctx.getExpectedTypeNames().isEmpty();
    }

    private boolean findEntrypoints() {
        entrypoints = new LinkedList<ChainElement>();
        final ASTNode node = ctx.getCompletionNode().orNull();
        if (node instanceof CompletionOnQualifiedNameReference) {
            invocationSite = (CompletionOnQualifiedNameReference) node;
            findEntrypointsForCompletionOnQualifiedName((CompletionOnQualifiedNameReference) node);
        } else if (node instanceof CompletionOnMemberAccess) {
            invocationSite = (CompletionOnMemberAccess) node;
            findEntrypointsForCompletionOnMemberAccess((CompletionOnMemberAccess) node);
        } else if (node instanceof CompletionOnSingleNameReference
                || node instanceof CompletionOnQualifiedAllocationExpression
                || node instanceof CompletionOnMessageSend) {
            invocationSite = (InvocationSite) node;
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
            addPublicStaticMembersToEntrypoints((TypeBinding) b);
            break;
        case Binding.FIELD:
            addPublicInstanceMembersToEntrypoints(((FieldBinding) b).type);
            break;
        case Binding.LOCAL:
            addPublicInstanceMembersToEntrypoints(((VariableBinding) b).type);
            break;
        default:
            log(WARNING_CANNOT_HANDLE_FOR_FINDING_ENTRY_POINTS, b);
        }
    }

    private void addPublicStaticMembersToEntrypoints(final TypeBinding type) {
        for (final Binding m : findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods(type, invocationSite,
                scope)) {
            if (matchesExpectedPrefix(m)) {
                entrypoints.add(new ChainElement(m, false));
            }
        }
    }

    private void addPublicInstanceMembersToEntrypoints(final TypeBinding type) {
        for (final Binding m : findVisibleInstanceFieldsAndRelevantInstanceMethods(type, invocationSite, scope)) {
            if (matchesExpectedPrefix(m)) {
                entrypoints.add(new ChainElement(m, false));
            }
        }
    }

    private boolean matchesExpectedPrefix(final Binding binding) {
        return String.valueOf(binding.readableName()).startsWith(ctx.getPrefix());
    }

    private void findEntrypointsForCompletionOnMemberAccess(final CompletionOnMemberAccess node) {
        final TypeBinding b = node.actualReceiverType;
        if (b == null) {
            return;
        }
        addPublicInstanceMembersToEntrypoints(b);
    }

    private void findEntrypointsForCompletionOnSingleName() {
        InternalCompletionContext context = ctx.get(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, null);
        ObjectVector visibleLocalVariables = context.getVisibleLocalVariables();
        Set<String> localVariableNames = getLocalVariableNames(visibleLocalVariables);
        resolveEntrypoints(visibleLocalVariables, localVariableNames);
        resolveEntrypoints(context.getVisibleFields(), localVariableNames);
        resolveEntrypoints(context.getVisibleMethods(), localVariableNames);
    }

    private static Set<String> getLocalVariableNames(final ObjectVector visibleLocalVariables) {
        final Set<String> names = new HashSet<>();
        for (int i = visibleLocalVariables.size(); i-- > 0;) {
            final LocalVariableBinding decl = (LocalVariableBinding) visibleLocalVariables.elementAt(i);
            names.add(Arrays.toString(decl.name));
        }
        return names;
    }

    private void resolveEntrypoints(final ObjectVector elements, final Set<String> localVariableNames) {
        for (int i = elements.size(); i-- > 0;) {
            final Binding decl = (Binding) elements.elementAt(i);
            if (!matchesPrefixToken(decl)) {
                continue;
            }
            final String key = String.valueOf(decl.computeUniqueKey());
            if (key.startsWith("Ljava/lang/Object;")) { //$NON-NLS-1$
                continue;
            }
            boolean requiresThis = false;
            if (decl instanceof FieldBinding) {
                requiresThis = localVariableNames.contains(Arrays.toString(((FieldBinding) decl).name));
            }
            final ChainElement e = new ChainElement(decl, requiresThis);
            if (e.getReturnType() != null) {
                entrypoints.add(e);
            }
        }
    }

    private boolean matchesPrefixToken(final Binding decl) {
        return String.valueOf(decl.readableName()).startsWith(ctx.getPrefix());
    }

    private List<ICompletionProposal> executeCallChainSearch() {
        final int maxChains = prefStore.getInt(ChainsPreferencePage.PREF_MAX_CHAINS);
        final int minDepth = prefStore.getInt(ChainsPreferencePage.PREF_MIN_CHAIN_LENGTH);
        final int maxDepth = prefStore.getInt(ChainsPreferencePage.PREF_MAX_CHAIN_LENGTH);
        final String[] excludedTypes = prefStore.getString(ChainsPreferencePage.PREF_IGNORED_TYPES).split("\\|"); //$NON-NLS-1$
        for (int i = 0; i < excludedTypes.length; ++i) {
            excludedTypes[i] = "L" + excludedTypes[i].replace('.', '/'); //$NON-NLS-1$
        }

        final List<Optional<TypeBinding>> expectedTypes = TypeBindingAnalyzer.resolveBindingsForExpectedTypes(ctx,
                scope);
        final ChainFinder finder = new ChainFinder(expectedTypes, Sets.newHashSet(excludedTypes), invocationSite,
                scope);
        try {
            new SimpleTimeLimiter().callWithTimeout(new Callable<Void>() {
                @Override
                public Void call() {
                    finder.startChainSearch(entrypoints, maxChains, minDepth, maxDepth);
                    return null;
                }
            }, prefStore.getInt(ChainsPreferencePage.PREF_TIMEOUT), TimeUnit.SECONDS, true);
        } catch (final Exception e) {
            setError("Timeout during call chain computation."); //$NON-NLS-1$
        }
        return buildCompletionProposals(finder.getChains());
    }

    private List<ICompletionProposal> buildCompletionProposals(final List<Chain> chains) {
        final List<ICompletionProposal> proposals = new LinkedList<>();
        for (final Chain chain : chains) {
            final TemplateProposal proposal = CompletionTemplateBuilder.create(chain, ctx.getJavaContext());
            final ChainCompletionProposal completionProposal = new ChainCompletionProposal(proposal, chain);
            proposals.add(completionProposal);
        }
        return proposals;
    }

    private void setError(final String errorMessage) {
        error = errorMessage;
    }

    @Override
    public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public void sessionStarted() {
        setError(null);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public void sessionEnded() {
    }
}
