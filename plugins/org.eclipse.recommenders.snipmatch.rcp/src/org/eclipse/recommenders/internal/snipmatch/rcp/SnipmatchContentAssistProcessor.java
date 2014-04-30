/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch, Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.rcp.RcpPlugin.logError;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.ElementTypeResolver;
import org.eclipse.jdt.internal.corext.template.java.ImportsResolver;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.LinkResolver;
import org.eclipse.jdt.internal.corext.template.java.NameResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jdt.internal.corext.template.java.VarResolver;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class SnipmatchContentAssistProcessor implements IContentAssistProcessor {

    private static final String CONTEXT_ID = "SnipMatch-Java-Context"; //$NON-NLS-1$

    private final Set<ISnippetRepository> repos;
    private final TemplateContextType contextType;
    private final Image image;

    private JavaContentAssistInvocationContext ctx;
    private String terms;

    @Inject
    public SnipmatchContentAssistProcessor(Set<ISnippetRepository> repos, SharedImages images) {
        this.repos = repos;
        contextType = createContextType();
        image = images.getImage(SharedImages.Images.OBJ_BULLET_BLUE);
    }

    private TemplateContextType createContextType() {

        JavaContextType contextType = new JavaContextType();
        contextType.setId(CONTEXT_ID);
        contextType.initializeContextTypeResolvers();

        ImportsResolver importResolver = new ImportsResolver();
        importResolver.setType("import"); //$NON-NLS-1$
        contextType.addResolver(importResolver);

        VarResolver varResolver = new VarResolver();
        varResolver.setType("var"); //$NON-NLS-1$
        contextType.addResolver(varResolver);

        TypeResolver typeResolver = new TypeResolver();
        typeResolver.setType("newType"); //$NON-NLS-1$
        contextType.addResolver(typeResolver);

        LinkResolver linkResolver = new LinkResolver();
        linkResolver.setType("link"); //$NON-NLS-1$
        contextType.addResolver(linkResolver);

        NameResolver nameResolver = new NameResolver();
        nameResolver.setType("newName"); //$NON-NLS-1$
        contextType.addResolver(nameResolver);

        ElementTypeResolver elementTypeResolver = new ElementTypeResolver();
        elementTypeResolver.setType("elemType"); //$NON-NLS-1$
        contextType.addResolver(elementTypeResolver);

        return contextType;
    }

    public void setContext(JavaContentAssistInvocationContext ctx) {
        this.ctx = ctx;
    }

    public void setTerms(String query) {
        terms = query;

    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        if (StringUtils.isEmpty(terms)) {
            return new ICompletionProposal[0];
        }
        LinkedList<ICompletionProposal> proposals = Lists.newLinkedList();
        List<Recommendation<ISnippet>> recommendations = Lists.newArrayList();
        for (ISnippetRepository repo : repos) {
            recommendations.addAll(repo.search(terms));
        }
        ICompilationUnit cu = ctx.getCompilationUnit();
        IEditorPart editor = EditorUtility.isOpenInEditor(cu);
        for (Recommendation<ISnippet> recommendation : recommendations) {
            ISnippet snippet = recommendation.getProposal();
            ISourceViewer sourceViewer = (ISourceViewer) editor.getAdapter(ITextOperationTarget.class);
            Point range = sourceViewer.getSelectedRange();
            Template template = new Template(snippet.getName(), Joiner.on(", ").join(snippet.getTags()), CONTEXT_ID, //$NON-NLS-1$
                    snippet.getCode(), true);
            IRegion region = new Region(range.x, range.y);
            Position p = new Position(range.x, range.y);
            JavaContext ctx = new JavaContext(contextType, sourceViewer.getDocument(), p, cu);

            try {
                proposals.add(SnippetProposal.newSnippetProposal(snippet, template, ctx, region, image));
            } catch (Exception e) {
                logError(e, Constants.BUNDLE_ID, Messages.ERROR_CREATING_SNIPPET_PROPOSAL_FAILED);
            }
        }
        return Iterables.toArray(proposals, ICompletionProposal.class);
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }
}
