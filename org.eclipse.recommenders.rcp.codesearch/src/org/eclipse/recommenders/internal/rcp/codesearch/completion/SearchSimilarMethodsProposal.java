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
import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendCodeSearchRequestJob;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public final class SearchSimilarMethodsProposal extends JavaCompletionProposal {
    private static final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/search.png").createImage();
    private final MethodDeclaration method;
    private final CompilationUnit recCu;
    private final IIntelligentCompletionContext ctx;
    private final CodeSearchClient searchClient;

    public SearchSimilarMethodsProposal(final int relevance, final CompilationUnit cu,
            final IIntelligentCompletionContext ctx, final CodeSearchClient searchClient) {
        super("", getOffSet(ctx), 0, icon, createTitle(ctx.getEnclosingMethod()), relevance);
        recCu = cu;
        this.ctx = ctx;
        this.searchClient = searchClient;
        method = recCu.findMethod(ctx.getEnclosingMethod());
    }

    private static String createTitle(final IMethodName name) {
        final String simpleName = Names.vm2srcSimpleMethod(name);
        return format("Search methods similar to '%s'", simpleName);
    }

    private static int getOffSet(final IIntelligentCompletionContext ctx) {
        return ctx.getOriginalContext().getInvocationOffset();
    }

    @Override
    public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
        return "Searches snippets similar to " + Names.vm2srcSimpleMethod(method.name) + "...";
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        try {
            final Request request = createRequestFromEnclosingMethod();
            request.issuedBy = UUIDHelper.getUUID();
            scheduleSearchRequest(request);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleSearchRequest(final Request request) {
        new SendCodeSearchRequestJob(request, ctx.getOriginalContext().getProject(), searchClient).schedule();
    }

    private Request createRequestFromEnclosingMethod() throws JavaModelException, PartInitException {
        final JavaEditor editor = (JavaEditor) JavaUI.openInEditor(ctx.getCompilationUnit());
        final ASTNode node = CrASTUtil.resolveClosestMethodOrTypeDeclarationNode(editor);
        if (node != null) {
            return new SimilarMethodsSearchRequestCreator((org.eclipse.jdt.core.dom.MethodDeclaration) node)
                    .getRequest();
            // final SearchRequestCreator searchRequestCreator = new
            // SearchRequestCreator(node, selection);
            // return searchRequestCreator.getRequest();
        }
        throw throwUnreachable("unexpected selection - please report this issue.");
    }
}
