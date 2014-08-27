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
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.completion.rcp.tips.ICompletionTipProposal;
import org.eclipse.recommenders.internal.completion.rcp.Constants;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TipsSessionProcessor extends SessionProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TipsSessionProcessor.class);

    private static final String PREF_NODE_ID_TIPS = "org.eclipse.recommenders.completion.rcp"; //$NON-NLS-1$
    private static final String SEEN = "completion_tips_seen"; //$NON-NLS-1$

    private static final String COMPLETION_TIP_ID = "id"; //$NON-NLS-1$
    private static final String COMPLETION_TIP_CLASS = "class"; //$NON-NLS-1$

    private final Map<ICompletionTipProposal, String> unseenTips = Maps.newHashMap();
    private final HashSet<String> seenTips;

    private boolean tipsSeen;

    public TipsSessionProcessor() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                Constants.EXT_POINT_COMPLETION_TIPS);

        Iterable<String> split = Splitter.on(':').omitEmptyStrings().split(getTipsPreferences().get(SEEN, "")); //$NON-NLS-1$
        seenTips = Sets.newHashSet(split);

        for (final IConfigurationElement element : elements) {
            String id = element.getAttribute(COMPLETION_TIP_ID);
            if (!seenTips.contains(id)) {
                try {
                    ICompletionTipProposal proposal = (ICompletionTipProposal) element
                            .createExecutableExtension(COMPLETION_TIP_CLASS);
                    unseenTips.put(proposal, id);
                } catch (CoreException e) {
                    LOG.error("Cannot instantiate completion tip", e); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        if (unseenTips.isEmpty()) {
            return false;
        }

        if (preventsAutoComplete(context)) {
            return false;
        }

        for (ICompletionTipProposal tip : unseenTips.keySet()) {
            tip.setCursorPosition(context.getInvocationOffset());
        }

        return true;
    }

    private boolean preventsAutoComplete(IRecommendersCompletionContext context) {
        return context.getProposals().size() <= 1;
    }

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        proposals.addAll(Collections2.filter(unseenTips.keySet(), new Predicate<ICompletionTipProposal>() {

            @Override
            public boolean apply(ICompletionTipProposal input) {
                return input.isApplicable();
            }
        }));
        tipsSeen = false;
    }

    @Override
    public void selected(ICompletionProposal proposal) {
        if (unseenTips.containsKey(proposal)) {
            String id = unseenTips.remove(proposal);
            seenTips.add(id);
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
            LOG.error("Failed to flush preferences", e); //$NON-NLS-1$
        }
    }

    private static IEclipsePreferences getTipsPreferences() {
        return InstanceScope.INSTANCE.getNode(PREF_NODE_ID_TIPS);
    }
}
