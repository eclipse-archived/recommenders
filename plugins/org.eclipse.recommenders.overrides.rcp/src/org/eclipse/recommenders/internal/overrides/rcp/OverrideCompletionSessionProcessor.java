/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.overrides.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;
import static org.eclipse.recommenders.utils.Recommendations.asPercentage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnFieldType;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.internal.overrides.rcp.l10n.LogMessages;
import org.eclipse.recommenders.internal.overrides.rcp.l10n.Messages;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.overrides.IOverrideModel;
import org.eclipse.recommenders.overrides.IOverrideModelProvider;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ProposalMatcher;
import org.eclipse.recommenders.utils.names.VmMethodName;

@SuppressWarnings({ "restriction" })
public class OverrideCompletionSessionProcessor extends SessionProcessor {

    private final Provider<IProjectCoordinateProvider> pcProvider;
    private final Provider<IOverrideModelProvider> modelProvider;
    private final IProposalNameProvider methodNameProvider;
    private final JavaElementResolver jdtCache;
    private final OverridesRcpPreferences prefs;
    private final OverlayImageProposalProcessor overlayProcessor;

    private IRecommendersCompletionContext ctx;
    private IType enclosingType;
    private IType supertype;
    private ProjectCoordinate pc;
    private IOverrideModel model;
    private List<Recommendation<IMethodName>> recommendations;

    @Inject
    public OverrideCompletionSessionProcessor(Provider<IProjectCoordinateProvider> pcProvider,
            Provider<IOverrideModelProvider> modelProvider, IProposalNameProvider methodNameProvider,
            JavaElementResolver cache, SharedImages images, OverridesRcpPreferences prefs) {
        this.pcProvider = pcProvider;
        this.modelProvider = modelProvider;
        this.methodNameProvider = methodNameProvider;
        this.jdtCache = cache;
        this.prefs = prefs;
        this.overlayProcessor = new OverlayImageProposalProcessor(images.getDescriptor(OVR_STAR), IDecoration.TOP_LEFT);
    };

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        recommendations = null;
        ctx = context;

        if (isSupportedCompletionType() && findEnclosingType() && findSuperclass() && findProjectCoordinate()
                && hasModel()) {
            try {
                computeRecommendations();
                return true;
            } catch (Exception e) {
                Logs.log(LogMessages.ERROR_FAILED_TO_COMPUTE_OVERRIDE_RECOMMENDATIONS, e);
            } finally {
                releaseModel();
            }
        }
        return false;
    }

    private boolean isSupportedCompletionType() {
        ASTNode completionNode = ctx.getCompletionNode().orNull();
        return completionNode != null && completionNode.getClass() == CompletionOnFieldType.class;
    }

    private boolean findEnclosingType() {
        enclosingType = ctx.getEnclosingType().orNull();
        return enclosingType != null;
    }

    private boolean findSuperclass() {
        supertype = JdtUtils.findSuperclass(enclosingType).orNull();
        return supertype != null;
    }

    private boolean findProjectCoordinate() {
        pc = pcProvider.get().resolve(supertype).orNull();
        return pc != null;
    }

    private boolean hasModel() {
        UniqueTypeName name = new UniqueTypeName(pc, jdtCache.toRecType(supertype));
        model = modelProvider.get().acquireModel(name).orNull();
        return model != null;
    }

    private void releaseModel() {
        if (model != null) {
            modelProvider.get().releaseModel(model);
        }
    }

    private void computeRecommendations() throws JavaModelException {
        for (final IMethod m : enclosingType.getMethods()) {
            final IMethod superMethod = JdtUtils.findOverriddenMethod(m).orNull();
            if (superMethod != null) {
                final IMethodName recSuperMethod = jdtCache.toRecMethod(superMethod).or(VmMethodName.NULL);
                model.setObservedMethod(recSuperMethod);
            }
        }
        recommendations = Recommendations.top(model.recommendOverrides(), prefs.maxNumberOfProposals,
                prefs.minProposalPercentage / 100d);
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        if (recommendations == null || recommendations.isEmpty()) {
            return;
        }
        CompletionProposal coreProposal = proposal.getCoreProposal().orNull();
        if (coreProposal == null) {
            return;
        }
        if (coreProposal.getKind() != CompletionProposal.METHOD_DECLARATION) {
            return;
        }
        IMethodName proposedMethod = methodNameProvider.toMethodName(coreProposal).orNull();
        if (proposedMethod == null) {
            return;
        }
        final ProposalMatcher matcher = new ProposalMatcher(proposedMethod);
        for (final Recommendation<IMethodName> recommendation : recommendations) {
            IMethodName recommendedMethod = recommendation.getProposal();
            if (!matcher.match(recommendedMethod)) {
                continue;
            }

            // XXX rather high value but otherwise the default constructor shows up between the overrides
            // proposals
            final int boost = prefs.changeProposalRelevance ? 1000 + asPercentage(recommendation) : 0;
            final String label = prefs.decorateProposalText
                    ? format(Messages.PROPOSAL_LABEL_PERCENTAGE, recommendation.getRelevance())
                    : ""; //$NON-NLS-1$

            if (boost > 0) {
                // TODO Shouldn't this convey the real boost?
                proposal.setTag(RECOMMENDERS_SCORE, asPercentage(recommendation));
            }

            ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
            mgr.addProcessor(new SimpleProposalProcessor(boost, label));
            if (prefs.decorateProposalIcon) {
                mgr.addProcessor(overlayProcessor);
            }
            break;
        }
    }
}
