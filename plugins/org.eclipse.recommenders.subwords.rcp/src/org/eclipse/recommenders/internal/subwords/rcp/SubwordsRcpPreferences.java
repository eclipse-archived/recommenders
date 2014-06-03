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
package org.eclipse.recommenders.internal.subwords.rcp;

import static org.eclipse.recommenders.internal.subwords.rcp.Constants.*;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("restriction")
public class SubwordsRcpPreferences {

    @Inject
    @Preference(PREF_COMPREHENSIVE_SUBWORDS_MATCHING_TYPES)
    public boolean computeAdditionalTypeProposals;

    @Inject
    @Preference(PREF_COMPREHENSIVE_SUBWORDS_MATCHING_CONSTRUCTORS)
    public boolean computeAdditionalConstructorProposals;

    public SubwordsRcpPreferences() {
        this(true, true);
    }

    @VisibleForTesting
    public SubwordsRcpPreferences(boolean computeAdditionalTypeProposals, boolean computeAdditionalConstructorProposals) {
        this.computeAdditionalTypeProposals = computeAdditionalTypeProposals;
        this.computeAdditionalConstructorProposals = computeAdditionalConstructorProposals;
    }
}
