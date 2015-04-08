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

import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.RECOMMENDERS_SCORE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OVR_STAR;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.processable.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.processable.Proposals;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.processable.SimpleProposalProcessor;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class TypesCompletionSessionProcessor extends SessionProcessor {

    private static final CompletionProposal NULL_PROPOSAL = new CompletionProposal();

    private ImmutableSet<String> subtypes;

    private ImageDescriptor overlay;

    @Inject
    public TypesCompletionSessionProcessor(SharedImages images) {
        overlay = images.getDescriptor(OVR_STAR);

    }

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        Builder<String> b = ImmutableSet.builder();
        Set<ITypeName> expectedTypes = context.getExpectedTypeNames();
        TypesIndexService service = TypesIndexService.getInstance();
        for (ITypeName expected : expectedTypes) {
            b.addAll(service.subtypes(expected, context.getProject()));
        }
        subtypes = b.build();
        return !expectedTypes.isEmpty();
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        if (subtypes == null) {
            return;
        }
        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
            // parse the type name and remove Generics from the name
            char[] sig = coreProposal.getDeclarationSignature();
            String name = new String(sig, 1, sig.length - 2);
            name = substringBefore(name, "<");
            if (subtypes.contains(name)) {
                proposal.setTag(RECOMMENDERS_SCORE, 1);
                Proposals.overlay(proposal, overlay);
                ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
                mgr.addProcessor(new SimpleProposalProcessor(1));
            }
        }
    }

}
