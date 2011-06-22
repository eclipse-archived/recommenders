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
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class SubwordsFieldCastedCompletionProposal extends JavaFieldWithCastedReceiverCompletionProposal {

	public SubwordsFieldCastedCompletionProposal(final JavaFieldWithCastedReceiverCompletionProposal jdtProposal,
			final CompletionProposal originalProposal, final JavaContentAssistInvocationContext invocationContext) {
		super(jdtProposal.getDisplayString(), jdtProposal.getReplacementOffset(), jdtProposal.getReplacementLength(), jdtProposal.getImage(),
				jdtProposal.getStyledDisplayString(), jdtProposal.getRelevance(), true, invocationContext, originalProposal);
	}

	@Override
	protected boolean isPrefix(final String prefix, String completion) {
		return checkStringMatchesPrefixPattern(prefix, completion);
	}

}
