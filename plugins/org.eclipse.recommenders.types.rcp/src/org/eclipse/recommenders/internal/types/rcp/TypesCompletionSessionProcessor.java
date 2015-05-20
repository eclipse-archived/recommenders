/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import static org.apache.commons.lang3.StringUtils.*;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.OverlayImageProposalProcessor;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class TypesCompletionSessionProcessor extends SessionProcessor {

    private static final CompletionProposal NULL_PROPOSAL = new CompletionProposal();
    public static final int BOOST = 50;

    private ImmutableSet<String> subtypes;

    private final ITypesIndexService service;
    private final OverlayImageProposalProcessor overlayDecorator;

    @Inject
    public TypesCompletionSessionProcessor(ITypesIndexService service, SharedImages images) {
        this.service = service;
        overlayDecorator = new OverlayImageProposalProcessor(images.getDescriptor(OVR_STAR), IDecoration.TOP_LEFT);
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        Set<ITypeName> expectedTypes = context.getExpectedTypeNames();
        if (expectedTypes.isEmpty()) {
            return false;
        }

        Builder<String> b = ImmutableSet.builder();
        for (ITypeName expected : expectedTypes) {
            String oneCharPrefix = substring(context.getPrefix(), 0, 1);
            b.addAll(service.subtypes(expected, oneCharPrefix, context.getProject()));
        }
        subtypes = b.build();
        return !subtypes.isEmpty();
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        if (subtypes == null || subtypes.isEmpty()) {
            return;
        }
        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.TYPE_REF: {
            char[] sig = coreProposal.getSignature();
            handleProposal(proposal, sig);
            break;
        }
        case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
            char[] sig = coreProposal.getDeclarationSignature();
            handleProposal(proposal, sig);
        }
    }

    private void handleProposal(IProcessableProposal proposal, char[] signature) {
        // parse the type name and remove Generics from the name
        String name = new String(signature, 1, signature.length - 2);
        name = substringBefore(name, "<");

        if (subtypes.contains(name)) {
            proposal.setTag(RECOMMENDERS_SCORE, BOOST);
            ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
            mgr.addProcessor(new SimpleProposalProcessor(BOOST));
            mgr.addProcessor(overlayDecorator);
        }
    }

}
