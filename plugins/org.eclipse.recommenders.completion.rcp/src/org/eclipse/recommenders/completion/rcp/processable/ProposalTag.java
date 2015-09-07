/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;

public enum ProposalTag implements IProposalTag {
    /**
     * Key to access the {@link IRecommendersCompletionContext} of the current session.
     */
    CONTEXT,
    /**
     * Key to access the original (unmodified) JDT proposal which was used to create this processable proposal.
     */
    JDT_UI_PROPOSAL,
    /**
     * Key to access the original (unmodified) JDT core completion proposal.
     */
    JDT_CORE_PROPOSAL,
    /**
     * Key to access the original integer score JDT UI assigned to this proposal.
     */
    JDT_SCORE,
    /**
     * Key to access the integer score (usually the percentage value) code recommenders engines assigned to this
     * proposal.
     */
    RECOMMENDERS_SCORE,
    /**
     * Key to access the score subwords assigned this processable proposal.
     */
    SUBWORDS_SCORE,
    /**
     * Key to access a boolean value indicating whether the given proposal was a camel case match.
     */
    IS_CAMEL_CASE_MATCH,
    /**
     * Key to access a boolean value indicating whether the given proposal was an exact match.
     */
    IS_EXACT_MATCH,
    /**
     * Key to access a boolean value indicating whether the given proposal was a pure prefix match.
     */
    IS_PREFIX_MATCH,
    /**
     * Key to access a boolean value indicating whether the given proposal was a prefix match only when ignoring casing.
     */
    IS_CASE_INSENSITIVE_PREFIX_MATCH,
    /**
     * Key to access a boolean value indicating whether the given proposal was visible to the user until the end of the
     * completion session.
     */
    IS_VISIBLE,
    /**
     * Key to access the active prefix of a proposal. This is similar to {@link IProcessableProposal#getPrefix()}
     */
    PREFIX
}
