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
package org.eclipse.recommenders.completion.rcp.processable;

import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.CONTEXT;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Optional;

public class Proposals {

    public static void overlay(IProcessableProposal proposal, ImageDescriptor icon) {
        Image originalImage = proposal.getImage();
        DecorationOverlayIcon decorator = new DecorationOverlayIcon(originalImage, icon, IDecoration.TOP_LEFT);
        proposal.setImage(decorator.createImage());
    }

    public static IRecommendersCompletionContext getContext(IProcessableProposal proposal) {
        return proposal.<IRecommendersCompletionContext>getTag(CONTEXT).orNull();
    }

    /**
     * Null-safe variant of {@link IProcessableProposal#getPrefix()} that returns the
     * {@link IRecommendersCompletionContext#getPrefix()} in case the proposal returns null.
     */
    public static String getPrefix(IProcessableProposal proposal) {
        String prefix = proposal.getPrefix();
        if (prefix == null) {
            prefix = getContext(proposal).getPrefix();
        }
        return prefix;
    }

    public static Optional<CompletionProposal> getCoreProposal(ICompletionProposal proposal) {
        if (proposal instanceof IProcessableProposal) {
            IProcessableProposal pp = (IProcessableProposal) proposal;
            return pp.getCoreProposal();
        }
        return Optional.absent();
    }

    public static <T> Optional<T> getTag(ICompletionProposal proposal, String tag) {
        if (proposal instanceof IProcessableProposal) {
            IProcessableProposal pp = (IProcessableProposal) proposal;
            for (IProposalTag t : pp.tags()) {
                if (t.name().equals(tag)) {
                    return pp.getTag(t);
                }
            }
        }
        return Optional.absent();
    }

    public static boolean isKindOneOf(ICompletionProposal proposal, int... kinds) {
        CompletionProposal cp = getCoreProposal(proposal).orNull();
        if (cp != null) {
            for (int kind : kinds) {
                if (cp.getKind() == kind) {
                    return true;
                }
            }
        }
        return false;
    }
}
