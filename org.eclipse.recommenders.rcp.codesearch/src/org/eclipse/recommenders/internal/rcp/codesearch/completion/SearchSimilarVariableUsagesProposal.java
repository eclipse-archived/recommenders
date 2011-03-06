/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.completion;

import static java.lang.String.format;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendCodeSearchRequestJob;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public final class SearchSimilarVariableUsagesProposal extends JavaCompletionProposal {
    private final Variable variable;
    private static final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/search.png").createImage();
    private final IJavaProject javaProject;
    private final MethodDeclaration method;
    private final CodeSearchClient searchClient;

    public SearchSimilarVariableUsagesProposal(final int relevance, final CompilationUnit cu,
            final IIntelligentCompletionContext ctx, final CodeSearchClient searchClient) {
        super("", getOffSet(ctx), 0, icon, createTitle(ctx), relevance);
        this.searchClient = searchClient;
        method = cu.findMethod(ctx.getEnclosingMethod());
        variable = Variable.create(ctx.getReceiverName(), ctx.getReceiverType(), ctx.getEnclosingMethod());
        javaProject = ctx.getCompilationUnit().getJavaProject();
    }

    private static String createTitle(final IIntelligentCompletionContext ctx) {
        final Variable var = ctx.getVariable();
        final ITypeName type = var.type;
        final String simpleName = type != null ? type.getClassName() : "<unknown type>";
        return format("Search usages similar to '%s'", simpleName);
    }

    private static int getOffSet(final IIntelligentCompletionContext ctx) {
        return ctx.getOriginalContext().getInvocationOffset();
    }

    @Override
    public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
        return "Searches for snippets using " + variable.type.getClassName() + "...";
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        final Request request = createRequestFromVariable();
        scheduleSearchRequest(request);
    }

    private void scheduleSearchRequest(final Request request) {
        new SendCodeSearchRequestJob(request, javaProject, searchClient).schedule();
    }

    private Request createRequestFromVariable() {
        final Request request = new Request();
        setUniqueIds(request);
        request.query = new SnippetSummary();
        addOverriddenMethod(request.query);
        addUsedMethods(request.query);
        addVariableType(request.query);
        return request;
    }

    private void setUniqueIds(final Request request) {
        request.issuedBy = UUIDHelper.getUUID();
    }

    private void addOverriddenMethod(final SnippetSummary query) {
        final IMethodName root = method.superDeclaration;
        if (root != null) {
            query.overriddenMethods.add(root);
        }
    }

    private void addVariableType(final SnippetSummary query) {
        query.usedTypes.add(variable.type);
    }

    private void addUsedMethods(final SnippetSummary query) {
        for (final IMethodName targetMethod : variable.getReceiverCalls()) {
            query.calledMethods.add(targetMethod);
        }
    }
}
