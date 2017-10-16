/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.statics.rcp;

import static java.lang.Math.max;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;
import static org.eclipse.recommenders.utils.Recommendations.*;

import java.text.DecimalFormat;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.statics.IStaticsModel;
import org.eclipse.recommenders.statics.IStaticsModelProvider;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.Names;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class StaticsCompletionSessionProcessor extends SessionProcessor {

    private final DecimalFormat percentFormatter = new DecimalFormat("0%");
    private final DecimalFormat permilFormatter = new DecimalFormat("0.00%");

    private final Provider<IProjectCoordinateProvider> pcProvider;
    private final IStaticsModelProvider modelProvider;
    private final IProposalNameProvider methodNameProvider;
    private final StaticsRcpPreferences prefs;
    private final OverlayImageProposalProcessor overlayProcessor;

    private Map<IMethodName, Double> recommendations;

    @Inject
    public StaticsCompletionSessionProcessor(Provider<IProjectCoordinateProvider> pcProvider,
            IStaticsModelProvider modelProvider, IProposalNameProvider methodNameProvider, SharedImages images,
            StaticsRcpPreferences prefs) {
        this.pcProvider = pcProvider;
        this.modelProvider = modelProvider;
        this.methodNameProvider = methodNameProvider;
        this.prefs = prefs;
        this.overlayProcessor = new OverlayImageProposalProcessor(images.getDescriptor(OVR_STAR), IDecoration.TOP_LEFT);
    }

    @SuppressWarnings("restriction")
    @Override
    public boolean startSession(final IRecommendersCompletionContext context) {
        Multimap<TypeBinding, CompletionProposal> proposals = HashMultimap.create();
        recommendations = Maps.newHashMap();

        for (CompletionProposal proposal : context.getProposals().values()) {
            if (!isStaticMethodCallInternalCompletionProposal(proposal)) {
                continue;
            }
            InternalCompletionProposal internalProposal = (InternalCompletionProposal) proposal;
            Binding binding = internalProposal.getBinding();
            if (!(binding instanceof MethodBinding)) {
                continue;
            }
            MethodBinding methodBinding = (MethodBinding) binding;
            if (methodBinding.declaringClass == null) {
                continue;
            }
            proposals.put(methodBinding.declaringClass, proposal);
        }

        for (TypeBinding typeBinding : proposals.keySet()) {
            IType type = JdtUtils.createUnresolvedType(typeBinding).orNull();
            if (type == null) {
                continue;
            }
            Result<UniqueTypeName> result = pcProvider.get().tryToUniqueName(type);
            if (!result.isPresent()) {
                continue;
            }
            UniqueTypeName uniqueTypeName = result.get();
            IStaticsModel model = modelProvider.acquireModel(uniqueTypeName).orNull();
            if (model == null) {
                continue;
            }
            IJavaElement enclosingMethod = context.getEnclosingElement().orNull();
            if (enclosingMethod != null) {
                switch (enclosingMethod.getElementType()) {
                case IJavaElement.METHOD:
                    IMethodName enclosingMethodName = pcProvider.get().toName((IMethod) enclosingMethod).orNull();
                    model.setEnclosingMethod(enclosingMethodName);
                    break;
                case IJavaElement.INITIALIZER:
                    try {
                        int flags = ((IInitializer) enclosingMethod).getFlags();
                        enclosingMethodName = VmMethodName.get("Lsome",
                                Flags.isStatic(flags) ? "<clinit>()V" : "<init>()V");
                        model.setEnclosingMethod(enclosingMethodName);
                    } catch (JavaModelException e) {
                        // ignore
                    }
                    break;
                default:
                    break;
                }
            }
            Iterable<Recommendation<IMethodName>> recommendations = filterRelevance(
                    top(model.recommendCalls(), prefs.maxNumberOfProposals),
                    max(prefs.minProposalPercentage, 0.01) / 100);
            for (Recommendation<IMethodName> r : recommendations) {
                this.recommendations.put(r.getProposal(), r.getRelevance());
            }
            modelProvider.releaseModel(model);
        }
        return !proposals.isEmpty();
    }

    @SuppressWarnings("restriction")
    private boolean isStaticMethodCallInternalCompletionProposal(CompletionProposal p) {
        return p.getKind() == CompletionProposal.METHOD_REF && Flags.isStatic(p.getFlags())
                && p instanceof InternalCompletionProposal;
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        CompletionProposal coreProposal = proposal.getCoreProposal().orNull();
        if (coreProposal == null || coreProposal.getKind() != METHOD_REF) {
            return;
        }
        IMethodName methodName = methodNameProvider.toMethodName(coreProposal).orNull();
        if (methodName == null) {
            return;
        }
        Double p = recommendations.get(methodName);
        if (p == null) {
            return;
        }
        ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
        int score = p < 0.01d ? 0 : 1 + (int) Math.rint(p * 100);
        String text = p > 0.01d ? percentFormatter.format(p) : permilFormatter.format(p);
        mgr.addProcessor(new SimpleProposalProcessor(score, text));

        if (prefs.decorateProposalIcon) {
            mgr.addProcessor(overlayProcessor);
        }
    }
}
