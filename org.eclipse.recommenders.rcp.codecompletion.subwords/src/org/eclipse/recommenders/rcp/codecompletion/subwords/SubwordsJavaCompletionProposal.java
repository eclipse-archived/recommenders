/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import static org.eclipse.recommenders.rcp.codecompletion.subwords.RegexUtil.checkStringMatchesPrefixPattern;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class SubwordsJavaCompletionProposal extends JavaCompletionProposal {

	public SubwordsJavaCompletionProposal(final JavaCompletionProposal jdtProposal, final CompletionProposal proposal,
			final JavaContentAssistInvocationContext invocationContext) {
		super(jdtProposal.getReplacementString(), proposal.getReplaceStart(), jdtProposal.getReplacementLength(), jdtProposal.getImage(), jdtProposal
				.getStyledDisplayString(), jdtProposal.getRelevance(), true, invocationContext);
	}

    @Override
	protected boolean isPrefix(final String prefix, String completion) {
        return checkStringMatchesPrefixPattern(prefix, completion);
	}

}
