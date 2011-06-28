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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class SubwordsAnonymousCompletionProposal extends AnonymousTypeCompletionProposal {

	public SubwordsAnonymousCompletionProposal(AnonymousTypeCompletionProposal jdtProposal, final CompletionProposal proposal,
			JavaContentAssistInvocationContext ctx) throws CoreException {
		super(ctx.getProject(), ctx.getCompilationUnit(), ctx, proposal.getReplaceStart(), jdtProposal.getReplacementLength(), String
				.valueOf(proposal.getCompletion()), jdtProposal.getStyledDisplayString(), String.valueOf(proposal.getDeclarationSignature()),
				((IType) ctx.getProject().findElement(new String(proposal.getDeclarationKey()), null)), jdtProposal.getRelevance());
	}

	@Override
	protected boolean isPrefix(final String prefix, String completion) {
		return checkStringMatchesPrefixPattern(prefix, completion);
	}

}
