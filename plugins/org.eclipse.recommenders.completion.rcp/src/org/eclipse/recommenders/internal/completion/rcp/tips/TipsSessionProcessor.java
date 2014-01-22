/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.tips;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.DiscoverCompletionProposal;
import org.eclipse.recommenders.rcp.SharedImages;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TipsSessionProcessor extends SessionProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TipsSessionProcessor.class);

    private static final String PREF_NODE_ID_TIPS = "org.eclipse.recommenders.completion.rcp.tips";
    private static final String SEEN = "seen";

    private final List<ICompletionProposal> unseenTips = Lists.newLinkedList();

    private final HashSet<String> seenTips;
    private boolean tipsSeen;

    @Inject
    public TipsSessionProcessor(SharedImages images) {
        ImmutableList<? extends ICompletionProposal> availableTips = ImmutableList.of(new DiscoverCompletionProposal(
                images));

        Iterable<String> split = Splitter.on(":").omitEmptyStrings().split(getTipsPreferences().get(SEEN, ""));
        seenTips = Sets.newHashSet(split);

        for (ICompletionProposal availableTip : availableTips) {
            if (!seenTips.contains(availableTip.getClass().getName())) {
                unseenTips.add(availableTip);
            }
        }
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        if (preventsAutoComplete(context)) {
            return false;
        }

        if (unseenTips.isEmpty()) {
            return false;
        }

        return true;
    }

    private boolean preventsAutoComplete(IRecommendersCompletionContext context) {
        return context.getProposals().size() <= 1;
    }

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        proposals.addAll(unseenTips);
        tipsSeen = false;
    }

    @Override
    public void selected(ICompletionProposal proposal) {
        if (unseenTips.remove(proposal)) {
            seenTips.add(proposal.getClass().getName());
            tipsSeen = true;
        }
    }

    @Override
    public void aboutToClose() {
        if (tipsSeen) {
            persistSeenTips(seenTips);
        }
    }

    private static void persistSeenTips(Set<String> seenTips) {
        String joined = Joiner.on(':').join(seenTips);
        IEclipsePreferences store = getTipsPreferences();
        store.put(SEEN, joined);
        try {
            store.flush();
        } catch (BackingStoreException e) {
            LOG.error("Failed to flush preferences", e);
        }
    }

    private static IEclipsePreferences getTipsPreferences() {
        return InstanceScope.INSTANCE.getNode(PREF_NODE_ID_TIPS);
    }
}
