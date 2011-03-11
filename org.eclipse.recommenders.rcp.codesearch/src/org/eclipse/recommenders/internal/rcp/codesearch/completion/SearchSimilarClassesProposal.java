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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendCodeSearchRequestJob;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public final class SearchSimilarClassesProposal extends JavaCompletionProposal {
    private final TypeDeclaration type;
    private static final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/search.png").createImage();
    private final CompilationUnit recCu;
    private final IIntelligentCompletionContext ctx;
    private final CodeSearchClient searchClient;

    public SearchSimilarClassesProposal(final int relevance, final CompilationUnit cu,
            final IIntelligentCompletionContext ctx, final CodeSearchClient searchClient) {
        super("", getOffSet(ctx), 0, icon, createTitle(cu.primaryType), relevance);
        recCu = cu;
        this.ctx = ctx;
        this.searchClient = searchClient;
        type = recCu.primaryType;
    }

    private static String createTitle(final TypeDeclaration type) {
        final ITypeName name = type.name;
        final String simpleName = name.getClassName();
        return format("Search classes similar to '%s'", simpleName);
    }

    private static int getOffSet(final IIntelligentCompletionContext ctx) {
        return ctx.getOriginalContext().getInvocationOffset();
    }

    @Override
    public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
        if (type == null) {
            return "Searches snippets similar to current class";
        }
        return "Searches snippets similar to " + Names.vm2srcSimpleTypeName(type.name) + "...";
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        try {
            final Request request = createRequestFromEnclosingType();
            request.issuedBy = UUIDHelper.getUUID();
            scheduleSearchRequest(request);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleSearchRequest(final Request request) {
        new SendCodeSearchRequestJob(request, ctx.getOriginalContext().getProject(), searchClient).schedule();
    }

    private Request createRequestFromEnclosingType() throws JavaModelException, PartInitException {
        final JavaEditor editor = (JavaEditor) JavaUI.openInEditor(ctx.getCompilationUnit());
        final ITextSelection selection = RCPUtils.getTextSelection(editor);
        final ASTNode node = CrASTUtil.resolveClosestTypeDeclarationNode(editor);
        if (node != null) {
            return new SearchRequestCreator(node, selection).getRequest();
        }
        throw throwUnreachable("unexpected selection - please report this issue.");
    }
}
