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

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.SNIPMATCH_CONTEXT_ID;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.models.IDependencyListener;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class SnipmatchContentAssistProcessor implements IContentAssistProcessor {

    private final Repositories repos;
    private final SnippetRepositoryConfigurations configs;
    private final IProjectCoordinateProvider pcProvider;
    private final IDependencyListener dependencyListener;
    private final Image image;
    private final TemplateContextType snipmatchContextType;

    private JavaContentAssistInvocationContext context;
    private String terms;

    @Inject
    public SnipmatchContentAssistProcessor(SnippetRepositoryConfigurations configs, Repositories repos,
            IProjectCoordinateProvider pcProvider, IDependencyListener dependencyListener, SharedImages images) {
        this.repos = repos;
        this.configs = configs;
        this.dependencyListener = dependencyListener;
        this.pcProvider = pcProvider;
        image = images.getImage(SharedImages.Images.OBJ_BULLET_BLUE);
        snipmatchContextType = SnipmatchTemplateContextType.getInstance();
    }

    public void setContext(JavaContentAssistInvocationContext context) {
        this.context = context;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        if (StringUtils.isEmpty(terms)) {
            return new ICompletionProposal[0];
        }

        JavaEditorSearchContext searchContext = new JavaEditorSearchContext(terms, context, dependencyListener,
                pcProvider);

        LinkedList<ICompletionProposal> proposals = Lists.newLinkedList();

        List<SnippetRepositoryConfiguration> sortedConfigs = Lists.newArrayList();
        sortedConfigs.addAll(configs.getRepos());

        Collections.sort(sortedConfigs, new Comparator<SnippetRepositoryConfiguration>() {
            @Override
            public int compare(SnippetRepositoryConfiguration o1, SnippetRepositoryConfiguration o2) {
                return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        Point selection = viewer.getSelectedRange();
        IRegion region = new Region(selection.x, selection.y);
        Position p = new Position(selection.x, selection.y);
        IDocument document = viewer.getDocument();

        String selectedText = null;
        if (selection.y != 0) {
            try {
                selectedText = document.get(selection.x, selection.y);
            } catch (BadLocationException e) {
            }
        }

        ICompilationUnit cu = context.getCompilationUnit();
        JavaContext javaContext = new JavaContext(snipmatchContextType, document, p, cu);
        javaContext.setVariable("selection", selectedText); //$NON-NLS-1$
        javaContext.setForceEvaluation(true);

        for (int repositoryPriority = 0; repositoryPriority < sortedConfigs.size(); repositoryPriority++) {
            Optional<ISnippetRepository> repo = repos.getRepository(sortedConfigs.get(repositoryPriority).getId());

            if (repo.isPresent()) {
                List<Recommendation<ISnippet>> recommendations = repo.get().search(searchContext);
                if (!recommendations.isEmpty()) {
                    proposals.add(new RepositoryProposal(sortedConfigs.get(repositoryPriority), repositoryPriority,
                            recommendations.size()));
                    for (Recommendation<ISnippet> recommendation : recommendations) {
                        ISnippet snippet = recommendation.getProposal();

                        Template template = new Template(snippet.getName(), snippet.getDescription(),
                                SNIPMATCH_CONTEXT_ID, snippet.getCode(), true);

                        try {
                            proposals.add(SnippetProposal.newSnippetProposal(recommendation, repositoryPriority,
                                    template, javaContext, region, image));
                        } catch (Exception e) {
                            log(LogMessages.ERROR_CREATING_SNIPPET_PROPOSAL_FAILED, e);
                        }
                    }
                }
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
