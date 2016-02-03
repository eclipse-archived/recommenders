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
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.SNIPMATCH_CONTEXT_ID;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
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
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.coordinates.IDependencyListener;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.LogMessages;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Location;
import org.eclipse.recommenders.snipmatch.SearchContext;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractContentAssistProcessor<T extends ContentAssistInvocationContext>
        implements IContentAssistProcessor {

    private final Repositories repos;
    private final SnippetRepositoryConfigurations configs;
    private final IProjectCoordinateProvider pcProvider;
    protected final IDependencyListener dependencyListener;
    private final Image contextLoadingImage;
    private final Image snippetImage;
    protected final TemplateContextType templateContextType;

    protected T context;
    protected Set<DependencyInfo> availableDependencies;
    private String terms;
    private ContextLoadingProposal contextLoadingProposal;
    private String filename;

    @Inject
    public AbstractContentAssistProcessor(TemplateContextType templateContextType,
            SnippetRepositoryConfigurations configs, Repositories repos, IProjectCoordinateProvider pcProvider,
            IDependencyListener dependencyListener, SharedImages images) {
        this.templateContextType = templateContextType;
        this.repos = repos;
        this.configs = configs;
        this.dependencyListener = dependencyListener;
        this.pcProvider = pcProvider;
        contextLoadingImage = images.getImage(SharedImages.Images.OBJ_HOURGLASS);
        snippetImage = images.getImage(SharedImages.Images.OBJ_BULLET_BLUE);
    }

    public void setContext(T context) {
        this.context = context;
        this.availableDependencies = calculateAvailableDependencies(context);
        if (!allProjectCoordinatesCached(pcProvider, availableDependencies)) {
            contextLoadingProposal = new ContextLoadingProposal(pcProvider, availableDependencies, contextLoadingImage);
            contextLoadingProposal.schedule();
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    protected abstract Set<DependencyInfo> calculateAvailableDependencies(T context);

    public void setTerms(String terms) {
        this.terms = terms;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        if (StringUtils.isEmpty(terms)) {
            return new ICompletionProposal[0];
        }

        Set<ProjectCoordinate> projectCoordinates = tryResolve(pcProvider, availableDependencies);

        SearchContext searchContext = new SearchContext(terms, getLocation(), filename, projectCoordinates);

        LinkedList<ICompletionProposal> proposals = Lists.newLinkedList();

        List<SnippetRepositoryConfiguration> sortedConfigs = Lists.newArrayList();
        sortedConfigs.addAll(configs.getRepos());

        Collections.sort(sortedConfigs, new Comparator<SnippetRepositoryConfiguration>() {
            @Override
            public int compare(SnippetRepositoryConfiguration o1, SnippetRepositoryConfiguration o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });

        Point selection = viewer.getSelectedRange();
        IRegion region = new Region(selection.x, selection.y);
        Position position = new Position(selection.x, selection.y);
        IDocument document = viewer.getDocument();

        String selectedText = null;
        if (selection.y != 0) {
            try {
                selectedText = document.get(selection.x, selection.y);
            } catch (BadLocationException e) {
            }
        }

        TemplateContext templateContext = getTemplateContext(document, position);
        templateContext.setVariable("selection", selectedText); //$NON-NLS-1$

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
                                    template, templateContext, region, snippetImage));
                        } catch (Exception e) {
                            log(LogMessages.ERROR_CREATING_SNIPPET_PROPOSAL_FAILED, e);
                        }
                    }
                }
            }
        }

        if (isResolvingProjectDependencies()) {
            proposals.add(contextLoadingProposal);
        }

        return Iterables.toArray(proposals, ICompletionProposal.class);
    }

    protected abstract Location getLocation();

    protected abstract TemplateContext getTemplateContext(IDocument document, Position position);

    private boolean isResolvingProjectDependencies() {
        return contextLoadingProposal != null && contextLoadingProposal.isStillLoading();
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

    private static boolean allProjectCoordinatesCached(IProjectCoordinateProvider pcProvider,
            Set<DependencyInfo> dependencyInfos) {
        for (DependencyInfo dependencyInfo : dependencyInfos) {
            Result<ProjectCoordinate> pc = pcProvider.tryResolve(dependencyInfo);
            if (pc.getReason() == org.eclipse.recommenders.utils.Constants.REASON_NOT_IN_CACHE) {
                return false;
            }
        }
        return true;
    }

    private static Set<ProjectCoordinate> tryResolve(IProjectCoordinateProvider pcProvider,
            Set<DependencyInfo> dependencyInfos) {
        Set<ProjectCoordinate> result = Sets.newHashSet();

        for (DependencyInfo dependencyInfo : dependencyInfos) {
            ProjectCoordinate pc = pcProvider.tryResolve(dependencyInfo).or(null);
            if (pc != null) {
                result.add(pc);
            }
        }

        return result;
    }
}
