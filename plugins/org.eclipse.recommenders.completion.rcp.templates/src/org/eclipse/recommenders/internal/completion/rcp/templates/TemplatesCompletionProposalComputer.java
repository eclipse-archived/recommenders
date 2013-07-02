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
package org.eclipse.recommenders.internal.completion.rcp.templates;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.CharUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.recommenders.utils.rcp.ast.MethodDeclarationFinder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Controls the process of template recommendations.
 */
@SuppressWarnings("restriction")
public class TemplatesCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private static final String CATEGORY_ID = "org.eclipse.recommenders.completion.rcp.templates.category";

    public static enum CompletionMode {
        TYPE_NAME, MEMBER_ACCESS, THIS
    }

    private final IRecommendersCompletionContextFactory ctxFactory;
    private IRecommendersCompletionContext rCtx;
    private IMethod enclosingMethod;
    private Set<IType> candidates;
    private String variableName;
    private boolean requiresConstructor;
    private String methodPrefix;
    private CompletionMode mode;
    private final IModelArchiveStore<IType, IObjectMethodCallsNet> store;
    private final JavaElementResolver elementResolver;
    private Image icon;

    @Inject
    public TemplatesCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory,
            final IModelArchiveStore<IType, IObjectMethodCallsNet> store, final JavaElementResolver elementResolver) {
        this.ctxFactory = ctxFactory;
        this.store = store;
        this.elementResolver = elementResolver;
        loadImage();
    }

    private void loadImage() {
        final Bundle bundle = FrameworkUtil.getBundle(TemplatesCompletionProposalComputer.class);
        icon = null;
        if (bundle != null) {
            final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(bundle.getSymbolicName(),
                    "metadata/icon2.gif");
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

        rCtx = ctxFactory.create((JavaContentAssistInvocationContext) context);
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
        String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
        Set<String> ex = Sets.newHashSet(excluded);
        if (!ex.contains(CATEGORY_ID)) {
            new DisableContentAssistCategoryJob(CATEGORY_ID).schedule();
            return false;
        }
        // we are not on the default tab
        return true;
    }

    private void addPatternsForType(final IType t, final ProposalBuilder proposalBuilder) {
        final Optional<IObjectMethodCallsNet> optModel = store.aquireModel(t);
        if (!optModel.isPresent()) {
            return;
        }
        final IObjectMethodCallsNet model = optModel.get();

        final ObjectUsage query = createQuery(t);
        model.setQuery(query);

        final List<Pair<String, Double>> callgroups = getMostLikelyPatternsSortedByProbability(model);
        for (final Pair<String, Double> p : callgroups) {
            final String patternId = p.getFirst();
            model.setPattern(patternId);
            for (final Pair<String, Double> def : model.getDefinitions()) {
                final Collection<IMethodName> calls = getCallsForDefinition(model, VmMethodName.get(def.getFirst()));
                calls.removeAll(query.calls);
                // patterns with less than two calls are no patterns :)
                if (calls.size() < 2) {
                    continue;
                }

                final PatternRecommendation pattern = new PatternRecommendation(patternId, model.getType(), calls,
                        p.getSecond());
                proposalBuilder.addPattern(pattern);
            }
        }

        store.releaseModel(model);
    }

    private Collection<IMethodName> getCallsForDefinition(final IObjectMethodCallsNet model,
            final IMethodName definition) {
        boolean constructorAdded = false;

        final TreeSet<IMethodName> calls = Sets.newTreeSet();
        final SortedSet<Pair<IMethodName, Double>> rec = model.getRecommendedMethodCalls(0.2d);
        if (rec.isEmpty()) {
            return Collections.emptyList();
        }
        if (requiresConstructor && definition.isInit()) {
            calls.add(definition);
            constructorAdded = true;
        }
        if (requiresConstructor && !constructorAdded) {
            return Collections.emptyList();
        }
        for (final Pair<IMethodName, Double> pair : rec) {
            calls.add(pair.getFirst());
        }
        if (!containsCallWithMethodPrefix(calls)) {
            return Collections.emptyList();
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

    private ObjectUsage createQuery(final IType t) {
        final ObjectUsage query = new ObjectUsage();
        query.type = elementResolver.toRecType(t);
        setContextOnQuery(query);
        query.kind = Kind.NEW;
        requiresConstructor = true;

        if (mode == CompletionMode.THIS || mode == CompletionMode.MEMBER_ACCESS) {
            final Optional<ObjectUsage> optUsage = findCompletionObjectUsage();
            if (optUsage.isPresent()) {
                final ObjectUsage usage = optUsage.get();
                query.calls = usage.calls;
                if (usage.kind != null) {
                    query.kind = usage.kind;
                    requiresConstructor = query.kind == Kind.UNKNOWN;
                }
                if (usage.definition != null) {
                    query.definition = usage.definition;
                    final Optional<IMethodName> def = rCtx.getMethodDef();
                    if (def.isPresent()) {
                        query.definition = def.get();
                        query.kind = Kind.METHOD_RETURN;
                        requiresConstructor = false;
                    }
                }
            }
        }

        return query;
    }

    private Optional<ObjectUsage> findCompletionObjectUsage() {
        final CompilationUnit ast = rCtx.getAST();
        final Optional<IMethod> enclosingMethod = rCtx.getEnclosingMethod();
        if (enclosingMethod.isPresent()) {
            final IMethod jdtMethod = enclosingMethod.get();
            final IMethodName recMethod = elementResolver.toRecMethod(jdtMethod).or(VmMethodName.NULL);
            final Optional<MethodDeclaration> astMethod = MethodDeclarationFinder.find(ast, recMethod);
            if (astMethod.isPresent()) {
                final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
                return Optional.of(r.findObjectUsage(variableName, astMethod.get()));
            }
        }
        return Optional.absent();
    }

    private void setContextOnQuery(final ObjectUsage query) {
        final Optional<IMethod> ovrMethod = JdtUtils.findOverriddenMethod(enclosingMethod);
        query.contextSuper = elementResolver.toRecMethod(ovrMethod.orNull()).orNull();

        final IMethod firstMethod = JdtUtils.findFirstDeclaration(enclosingMethod);
        query.contextFirst = elementResolver.toRecMethod(firstMethod).orNull();
    }

    private List<Pair<String, Double>> getMostLikelyPatternsSortedByProbability(final IObjectMethodCallsNet net) {
        final List<Pair<String, Double>> p = net.getPatternsWithProbability();

        Collections.sort(p, new Comparator<Pair<String, Double>>() {

            @Override
            public int compare(final Pair<String, Double> o1, final Pair<String, Double> o2) {
                return o2.getSecond().compareTo(o1.getSecond());
            }
        });
        return p;
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
            RecommendersPlugin.logError(e, "Failed to resolve super type of %s", rCtx.getEnclosingElement());
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
