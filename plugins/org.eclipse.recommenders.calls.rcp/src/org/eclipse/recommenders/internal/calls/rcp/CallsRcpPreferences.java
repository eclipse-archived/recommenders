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
package org.eclipse.recommenders.internal.calls.rcp;

import static org.eclipse.recommenders.internal.calls.rcp.Constants.*;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;

@SuppressWarnings("restriction")
public class CallsRcpPreferences {

    @Inject
    @Preference(PREF_MIN_PROPOSAL_PROBABILITY)
    public int minProposalProbability;

    @Inject
    @Preference(PREF_MAX_NUMBER_OF_PROPOSALS)
    public int maxNumberOfProposals;

    @Inject
    @Preference(PREF_UPDATE_PROPOSAL_RELEVANCE)
    public boolean changeProposalRelevance;

    @Inject
    @Preference(PREF_DECORATE_PROPOSAL_ICON)
    public boolean decorateProposalIcon;

    @Inject
    @Preference(PREF_DECORATE_PROPOSAL_TEXT)
    public boolean decorateProposalText;

    @Inject
    @Preference(PREF_HIGHLIGHT_USED_PROPOSALS)
    public boolean highlightUsedProposals;

    @Inject
    @Preference
    public IEclipsePreferences store;
}
