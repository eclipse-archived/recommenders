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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Controls the process of template recommendations.
 */
@SuppressWarnings("restriction")
public final class TemplatesCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private enum CompletionMode {
        TYPE_NAME, MEMBER_ACCESS, UNKNOWN, VAR_NAME
    }

    private AbstractJavaContextType templateContextType;
    private final IRecommendersCompletionContextFactory ctxFactory;
    private IRecommendersCompletionContext rCtx;
    private IMethod enclosingMethod;
    private IType receiverType;
    private Set<IType> candidates;
    private String receiverName;
    private String methodNamePrefix;
    private CompletionMode mode;
    private final IModelArchiveStore<IType, IObjectMethodCallsNet> store;
    private final JavaElementResolver jdtCache;

    /**
     * @param patternRecommender
     *            Computes and returns patterns suitable for the active completion request.
     * @param codeBuilder
     *            The {@link CodeBuilder} will turn MethodCalls into the code to be used by the eclipse template engine.
     * @param contextResolver
     *            Responsible for computing an {@link IIntelligentCompletionContext} from a
     *            {@link ContentAssistInvocationContext} which is given by the editor for each completion request.
     */
    @Inject
    public TemplatesCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory,
            IModelArchiveStore<IType, IObjectMethodCallsNet> store, JavaElementResolver jdtCache) {
        this.ctxFactory = ctxFactory;
        this.store = store;
        this.jdtCache = jdtCache;
        initializeTemplateContextType();
    }

    // /**
    // * Initializes the proposal builder.
    // *
    // * @param codeBuilder
    // * The {@link CodeBuilder} will turn {@link MethodCall}s into the code to be used by the eclipse template
    // * engine.
    // */
    // private void initializeProposalBuilder(final CodeBuilder codeBuilder) {
    // final Bundle bundle = FrameworkUtil.getBundle(TemplatesCompletionProposalComputer.class);
    // Image icon = null;
    // if (bundle != null) {
    // final ImageDescriptor desc = imageDescriptorFromPlugin(bundle.getSymbolicName(), "metadata/icon2.gif");
    // icon = desc.createImage();
    // }
    // completionProposalsBuilder = new CompletionProposalsBuilder(icon, codeBuilder);
    // }

    /**
     * Sets the appropriate <code>ContextType</code> for all computed templates.
     */
    private void initializeTemplateContextType() {
        final JavaPlugin plugin = JavaPlugin.getDefault();
        if (plugin != null) {
            templateContextType = (AbstractJavaContextType) plugin.getTemplateContextRegistry().getContextType(
                    JavaContextType.ID_ALL);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
        rCtx = ctxFactory.create((JavaContentAssistInvocationContext) context);
        if (!findEnclosingMethod()) {
            return Collections.emptyList();
        }

        findCompletionMode();
        if (mode != CompletionMode.TYPE_NAME) {
            return Collections.emptyList();
        }
        if (!findPotentialTypes()) {
            return Collections.emptyList();
        }

        for (IType t : candidates) {
            Optional<IObjectMethodCallsNet> opt = store.aquireModel(t);
            if (!opt.isPresent()) {
                continue;
            }
            IObjectMethodCallsNet net = opt.get();

            ObjectUsage query = new ObjectUsage();
            Optional<IMethod> ovrMethod = JdtUtils.findOverriddenMethod(enclosingMethod);
            query.contextSuper = jdtCache.toRecMethod(ovrMethod.orNull()).orNull();

            IMethod firstMethod = JdtUtils.findFirstDeclaration(enclosingMethod);
            query.contextFirst = jdtCache.toRecMethod(firstMethod).orNull();
            net.setQuery(query);

            double sum = 0.0d;

            DefinitionSite.Kind[] values = { DefinitionSite.Kind.NEW, DefinitionSite.Kind.METHOD_RETURN };

            for (DefinitionSite.Kind k : values) {
                query.kind = k;
                net.setQuery(query);
                for (Tuple<String, Double> p : getMostLikelyPatternsSortedByProbability(net)) {
                    String patternId = p.getFirst();
                    for (DefinitionSite def : net.getDefinitions()) {
                        double prob = p.getSecond();
                        if (prob < 0.05) {
                            break;
                        }

                        SortedSet<Tuple<IMethodName, Double>> rec = net.getRecommendedMethodCalls(0.2d);
                        if (rec.isEmpty()) {
                            continue;
                        }

                        TreeSet<IMethodName> calls = Sets.newTreeSet();
                        for (final Tuple<IMethodName, Double> pair : rec) {
                            calls.add(pair.getFirst());
                        }
                        System.out.printf("%3.3f %s\n", prob, calls);
                        continue;
                    }
                }
            }
            store.releaseModel(net);
        }

        // if (!findReceiverType()) {
        // return Collections.emptyList();
        // }
        // if (!findReceiverName()) {
        // return Collections.emptyList();
        // }
        // findRequiredMethodNamePrefix();
        return Collections.emptyList();
    }

    private List<Tuple<String, Double>> getMostLikelyPatternsSortedByProbability(IObjectMethodCallsNet net) {
        List<Tuple<String, Double>> p = net.getPatternsWithProbability();

        Collections.sort(p, new Comparator<Tuple<String, Double>>() {

            @Override
            public int compare(Tuple<String, Double> o1, Tuple<String, Double> o2) {
                return o2.getSecond().compareTo(o1.getSecond());
            }
        });
        return p;
    }

    private void findCompletionMode() {
        ASTNode n = rCtx.getCompletionNode();
        if (n instanceof CompletionOnSingleNameReference) {
            if (isPotentialClassName((CompletionOnSingleNameReference) n)) {
                mode = CompletionMode.TYPE_NAME;
            } else {
                mode = CompletionMode.VAR_NAME;
            }
        } else if (n instanceof CompletionOnMemberAccess) {
            mode = CompletionMode.MEMBER_ACCESS;
        } else {
            mode = CompletionMode.UNKNOWN;
        }
    }

    private boolean findPotentialTypes() {
        ASTNode n = rCtx.getCompletionNode();
        ASTNode p = rCtx.getCompletionNodeParent();
        CompletionOnSingleNameReference c = null;
        if (n instanceof CompletionOnSingleNameReference) {
            c = (CompletionOnSingleNameReference) n;
            candidates = findTypesBySimpleName(c.token);
        }
        return candidates != null;
    }

    private boolean isPotentialClassName(CompletionOnSingleNameReference c) {
        return c.token != null && c.token.length > 0 && CharUtils.isAsciiAlphaUpper(c.token[0]);
    }

    private void findRequiredMethodNamePrefix() {
        methodNamePrefix = rCtx.getPrefix();
    }

    private boolean findReceiverName() {
        receiverName = rCtx.getReceiverName();
        // actually, this is never null, right?
        return receiverName != null;
    }

    private boolean findReceiverType() {
        receiverType = rCtx.getReceiverType().orNull();
        return receiverType != null;
    }

    private boolean findEnclosingMethod() {
        enclosingMethod = rCtx.getEnclosingMethod().orNull();
        return enclosingMethod != null;

    }

    //
    // /**
    // * @param context
    // * The context from which the completion request was invoked.
    // * @return True, if the computer should try to find proposals for the given context.
    // */
    // private boolean shouldComputeProposalsForContext() {
    // if (!rCtx.getEnclosingMethod().isPresent()) {
    // return false;
    // }
    // if (!rCtx.getExpectedType().isPresent()) {
    // return !(rCtx.getCompletionNode() instanceof CompletionOnMemberAccess) || context.getVariable() != null
    // && context.getVariable().isThis();
    // }
    // return rCtx.getCompletionNode() instanceof CompletionOnLocalName
    // || rCtx.getCompletionNode() instanceof CompletionOnSingleNameReference;
    // }

    // /**
    // * @param rCtx
    // * The context from where the completion request was invoked.
    // * @return The completion proposals to be displayed in the editor.
    // */
    // public ImmutableList<? extends IJavaCompletionProposal> computeCompletionProposals() {
    // final CompletionTargetVariable completionTargetVariable = CompletionTargetVariableBuilder
    // .createInvokedVariable(rCtx);
    // if (completionTargetVariable == null) {
    // return ImmutableList.of();
    // }
    // return computeCompletionProposalsForTargetVariable(completionTargetVariable);
    // }
    //
    // /**
    // * @param completionTargetVariable
    // * The variable for which the completion proposals shall be created.
    // * @return The completion proposals to be displayed in the editor.
    // */
    // private ImmutableList<? extends IJavaCompletionProposal> computeCompletionProposalsForTargetVariable(
    // final CompletionTargetVariable completionTargetVariable) {
    // final Collection<PatternRecommendation> patternRecommendations = patternRecommender
    // .computeRecommendations(completionTargetVariable);
    // if (patternRecommendations.isEmpty()) {
    // return ImmutableList.of();
    // }
    // final DocumentTemplateContext templateContext = getTemplateContext(completionTargetVariable);
    // return completionProposalsBuilder.computeProposals(patternRecommendations, templateContext,
    // completionTargetVariable);
    //
    // }

    /**
     * @param completionTargetVariable
     *            The variable on which the completion request was executed.
     * @return A {@link DocumentTemplateContext} suiting the completion context.
     */
    private DocumentTemplateContext getTemplateContext(final CompletionTargetVariable completionTargetVariable) {
        final Region region = completionTargetVariable.getDocumentRegion();
        final IDocument document = rCtx.getJavaContext().getDocument();
        final JavaContext templateContext = new JavaContext(templateContextType, document, region.getOffset(),
                region.getLength(), rCtx.getCompilationUnit());
        templateContext.setForceEvaluation(true);
        return templateContext;
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
                public void acceptConstructor(int modifiers, char[] simpleTypeName, int parameterCount,
                        char[] signature, char[][] parameterTypes, char[][] parameterNames, int typeModifiers,
                        char[] packageName, int extraFlags, String path, AccessRestriction access) {
                }

                @Override
                public void acceptType(char[] packageName, char[] typeName, char[][] enclosingTypeNames, int modifiers,
                        AccessRestriction accessRestriction) {
                    IType res;
                    try {
                        res = project.findType(String.valueOf(packageName), String.valueOf(typeName));
                        if (res != null) {
                            result.add(res);
                        }
                    } catch (JavaModelException e) {
                        // TODO Auto-generated catch block
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
