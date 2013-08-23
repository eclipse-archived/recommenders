/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp.templates;

import static com.google.common.collect.Sets.newHashSet;
import static org.eclipse.recommenders.internal.calls.rcp.Constants.TEMPLATES_CATEGORY_ID;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.CharUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.calls.rcp.AstCallCompletionAnalyzer;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Controls the process of template recommendations.
 */
@SuppressWarnings("restriction")
public class TemplatesCompletionProposalComputer implements IJavaCompletionProposalComputer {
    private Logger log = LoggerFactory.getLogger(getClass());

    public static enum CompletionMode {
        TYPE_NAME, MEMBER_ACCESS, THIS
    }

    private IProjectCoordinateProvider pcProvider;
    private IRecommendersCompletionContext rCtx;
    private IMethod enclosingMethod;
    private Set<IType> candidates;
    private String variableName;
    private boolean requiresConstructor;
    private String methodPrefix;
    private CompletionMode mode;
    private final ICallModelProvider store;
    private final JavaElementResolver elementResolver;
    private Image icon;
    private ICallModel model;
    private AstCallCompletionAnalyzer completionAnalyzer;
    private IAstProvider astProvider;

    @Inject
    public TemplatesCompletionProposalComputer(final IProjectCoordinateProvider pcProvider,
            final ICallModelProvider store, IAstProvider astProvider, final JavaElementResolver elementResolver) {
        this.pcProvider = pcProvider;
        this.store = store;
        this.astProvider = astProvider;
        this.elementResolver = elementResolver;
        loadImage();
    }

    private void loadImage() {
        final Bundle bundle = FrameworkUtil.getBundle(TemplatesCompletionProposalComputer.class);
        icon = null;
        if (bundle != null) {
            final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(bundle.getSymbolicName(),
                    "icons/view16/templates-dots.gif");
            icon = desc.createImage();
        }
    }

    @VisibleForTesting
    public CompletionMode getCompletionMode() {
        return mode;
    }

    @VisibleForTesting
    public String getVariableName() {
        return variableName;
    }

    @VisibleForTesting
    public String getMethodPrefix() {
        return methodPrefix;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        if (!shouldMakeProposals()) {
            return Collections.EMPTY_LIST;
        }

        rCtx = new RecommendersCompletionContext((JavaContentAssistInvocationContext) context, astProvider);
        if (!findEnclosingMethod()) {
            return Collections.emptyList();
        }
        if (!findCompletionMode()) {
            return Collections.emptyList();
        }
        if (!findPotentialTypes()) {
            return Collections.emptyList();
        }

        final ProposalBuilder proposalBuilder = new ProposalBuilder(icon, rCtx, elementResolver, variableName);
        for (final IType t : candidates) {
            addPatternsForType(t, proposalBuilder);
        }
        return proposalBuilder.createProposals();
    }

    @VisibleForTesting
    protected boolean shouldMakeProposals() {
        // we only make proposals on non-default content assist lists (2nd, 3rd,...)
        String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
        Set<String> ex = Sets.newHashSet(excluded);
        if (!ex.contains(TEMPLATES_CATEGORY_ID)) {
            new DisableContentAssistCategoryJob(TEMPLATES_CATEGORY_ID).schedule();
            return false;
        }
        // we are not on the default tab
        return true;
    }

    private void addPatternsForType(final IType t, final ProposalBuilder proposalBuilder) {
        ProjectCoordinate pc = pcProvider.resolve(t).or(ProjectCoordinate.UNKNOWN);
        UniqueTypeName name = new UniqueTypeName(pc, elementResolver.toRecType(t));
        model = store.acquireModel(name).orNull();
        if (model == null) {
            return;
        }

        completionAnalyzer = new AstCallCompletionAnalyzer(rCtx);
        if (mode == CompletionMode.TYPE_NAME) {
            handleTypeNameCompletionRequest(proposalBuilder);
        }
        handleVariableCompletionRequest(proposalBuilder);
        store.releaseModel(model);
    }

    private void handleVariableCompletionRequest(final ProposalBuilder proposalBuilder) {
        completionAnalyzer.getReceiverType();
        // set override-context:
        IMethod overrides = completionAnalyzer.getOverridesContext().orNull();
        if (overrides != null) {
            IMethodName crOverrides = elementResolver.toRecMethod(overrides).or(
                    org.eclipse.recommenders.utils.Constants.UNKNOWN_METHOD);
            model.setObservedOverrideContext(crOverrides);
        }

        // set definition-type and defined-by
        model.setObservedDefinitionKind(completionAnalyzer.getReceiverDefinitionType());
        model.setObservedDefiningMethod(completionAnalyzer.getDefinedBy().orNull());

        // set calls:
        model.setObservedCalls(newHashSet(completionAnalyzer.getCalls()));

        final List<Recommendation<String>> callgroups = getMostLikelyPatternsSortedByProbability(model);
        for (final Recommendation<String> p : callgroups) {
            final String patternId = p.getProposal();
            model.setObservedPattern(patternId);
            for (final Recommendation<IMethodName> def : model.recommendDefinitions()) {
                final Collection<IMethodName> calls = getCallsForDefinition(model, def.getProposal());
                calls.removeAll(completionAnalyzer.getCalls());
                // patterns with less than two calls are no patterns :)
                if (calls.size() < 2) {
                    continue;
                }

                final PatternRecommendation pattern = new PatternRecommendation(patternId, model.getReceiverType(),
                        calls, p.getRelevance());
                proposalBuilder.addPattern(pattern);
            }
        }
    }

    private void handleTypeNameCompletionRequest(final ProposalBuilder proposalBuilder) {
        IMethod overrides = completionAnalyzer.getOverridesContext().orNull();
        model.setObservedDefinitionKind(DefinitionKind.NEW);
        if (overrides != null) {
            IMethodName crOverrides = elementResolver.toRecMethod(overrides).or(
                    org.eclipse.recommenders.utils.Constants.UNKNOWN_METHOD);
            model.setObservedOverrideContext(crOverrides);
        }

        final List<Recommendation<String>> callgroups = getMostLikelyPatternsSortedByProbability(model);
        for (final Recommendation<String> p : callgroups) {
            final String patternId = p.getProposal();
            model.setObservedPattern(patternId);
            for (final Recommendation<IMethodName> def : model.recommendDefinitions()) {
                IMethodName constructor = def.getProposal();
                if (!constructor.isInit()) {
                    continue;
                }
                final Collection<IMethodName> calls = getCallsForDefinition(model, constructor);
                calls.add(constructor);
                // patterns with less than two calls are no patterns :)
                if (calls.size() < 2) {
                    continue;
                }

                final PatternRecommendation pattern = new PatternRecommendation(patternId, model.getReceiverType(),
                        calls, p.getRelevance());
                proposalBuilder.addPattern(pattern);
            }
        }
    }

    private Collection<IMethodName> getCallsForDefinition(final ICallModel model, final IMethodName definition) {
        boolean constructorAdded = false;

        final TreeSet<IMethodName> calls = Sets.newTreeSet();
        final List<Recommendation<IMethodName>> rec = Recommendations.top(model.recommendCalls(), 100, 0.1d);
        if (rec.isEmpty()) {
            return Lists.newLinkedList();
        }
        if (requiresConstructor && definition.isInit()) {
            calls.add(definition);
            constructorAdded = true;
        }
        if (requiresConstructor && !constructorAdded) {
            return Lists.newLinkedList();
        }
        for (final Recommendation<IMethodName> pair : rec) {
            calls.add(pair.getProposal());
        }
        if (!containsCallWithMethodPrefix(calls)) {
            return Lists.newLinkedList();
        }
        return calls;
    }

    private boolean containsCallWithMethodPrefix(final TreeSet<IMethodName> calls) {
        for (final IMethodName call : calls) {
            if (call.getName().startsWith(methodPrefix)) {
                return true;
            }
        }
        return false;
    }

    private List<Recommendation<String>> getMostLikelyPatternsSortedByProbability(final ICallModel net) {
        return Recommendations.top(net.recommendPatterns(), 10, 0.03d);
    }

    private boolean findCompletionMode() {
        variableName = "";
        methodPrefix = "";
        mode = null;

        final ASTNode n = rCtx.getCompletionNode().orNull();
        if (n instanceof CompletionOnSingleNameReference) {
            if (isPotentialClassName((CompletionOnSingleNameReference) n)) {
                mode = CompletionMode.TYPE_NAME;
            } else {
                // eq$ --> receiver is this
                mode = CompletionMode.THIS;
                methodPrefix = rCtx.getReceiverName();
            }
        } else if (n instanceof CompletionOnQualifiedNameReference) {
            if (isPotentialClassName((CompletionOnQualifiedNameReference) n)) {
                mode = CompletionMode.TYPE_NAME;
            } else {
                mode = CompletionMode.MEMBER_ACCESS;
                variableName = rCtx.getReceiverName();
                methodPrefix = rCtx.getPrefix();
            }
        } else if (n instanceof CompletionOnMemberAccess) {
            final Expression ma = ((CompletionOnMemberAccess) n).receiver;
            if (ma.isImplicitThis() || ma.isSuper() || ma.isThis()) {
                mode = CompletionMode.THIS;
            } else {
                mode = CompletionMode.MEMBER_ACCESS;
            }
        }
        return mode != null;
    }

    private boolean findPotentialTypes() {
        if (mode == CompletionMode.TYPE_NAME) {
            final ASTNode n = rCtx.getCompletionNode().orNull();
            CompletionOnSingleNameReference c = null;
            if (n instanceof CompletionOnSingleNameReference) {
                c = (CompletionOnSingleNameReference) n;
                candidates = findTypesBySimpleName(c.token);
            }
        } else if (mode == CompletionMode.THIS) {
            createCandidatesFromOptional(getSupertypeOfThis());
        } else {
            createCandidatesFromOptional(rCtx.getReceiverType());
        }
        return candidates != null;
    }

    private Optional<IType> getSupertypeOfThis() {
        try {
            final IMethod m = rCtx.getEnclosingMethod().orNull();
            if (m == null || JdtFlags.isStatic(m)) {
                return Optional.absent();
            }
            final IType type = m.getDeclaringType();
            final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
            return Optional.of(hierarchy.getSuperclass(type));
        } catch (final Exception e) {
            log.error("Failed to resolve super type of " + rCtx.getEnclosingElement(), e);
            return Optional.absent();
        }
    }

    private void createCandidatesFromOptional(final Optional<IType> optType) {
        if (optType.isPresent()) {
            candidates = Sets.newHashSet(optType.get());
        }
    }

    private boolean isPotentialClassName(final CompletionOnQualifiedNameReference c) {
        final char[] name = c.completionIdentifier;
        return name != null && name.length > 0 && CharUtils.isAsciiAlphaUpper(name[0]);
    }

    private boolean isPotentialClassName(final CompletionOnSingleNameReference c) {
        return c.token != null && c.token.length > 0 && CharUtils.isAsciiAlphaUpper(c.token[0]);
    }

    private boolean findEnclosingMethod() {
        enclosingMethod = rCtx.getEnclosingMethod().orNull();
        return enclosingMethod != null;

    }

    @Override
    public void sessionStarted() {
        // This particular event is not of interest for us.
    }

    @Override
    public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
        // This particular event is not of interest for us.
    }

    public Set<IType> findTypesBySimpleName(final char[] simpleTypeName) {
        final Set<IType> result = Sets.newHashSet();
        try {
            final JavaProject project = (JavaProject) rCtx.getProject();
            final SearchableEnvironment environment = project
                    .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY);
            environment.findExactTypes(simpleTypeName, false, IJavaSearchConstants.TYPE, new ISearchRequestor() {
                @Override
                public void acceptConstructor(final int modifiers, final char[] simpleTypeName,
                        final int parameterCount, final char[] signature, final char[][] parameterTypes,
                        final char[][] parameterNames, final int typeModifiers, final char[] packageName,
                        final int extraFlags, final String path, final AccessRestriction access) {
                }

                @Override
                public void acceptType(final char[] packageName, final char[] typeName,
                        final char[][] enclosingTypeNames, final int modifiers,
                        final AccessRestriction accessRestriction) {
                    IType res;
                    try {
                        res = project.findType(String.valueOf(packageName), String.valueOf(typeName));
                        if (res != null) {
                            result.add(res);
                        }
                    } catch (final JavaModelException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void acceptPackage(final char[] packageName) {
                }
            });
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e);
        }
        return result;
    }
}
