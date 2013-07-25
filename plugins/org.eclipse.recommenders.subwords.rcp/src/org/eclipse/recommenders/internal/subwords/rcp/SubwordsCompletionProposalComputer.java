/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import javax.inject.Inject;

import org.eclipse.recommenders.completion.rcp.processable.ProcessableCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.ProcessableProposalFactory;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessor;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

public class SubwordsCompletionProposalComputer extends ProcessableCompletionProposalComputer {
    public static final String CATEGORY_ID = "org.eclipse.recommenders.subwords.rcp.category";

    @Inject
    public SubwordsCompletionProposalComputer(IAstProvider astProvider) {
        this(new SubwordsSessionProcessor(astProvider), astProvider);
    }

    @VisibleForTesting
    public SubwordsCompletionProposalComputer(SubwordsSessionProcessor processor, IAstProvider astProvider) {
        super(new ProcessableProposalFactory(), Sets.<SessionProcessor>newHashSet(processor), astProvider);
    }
}
