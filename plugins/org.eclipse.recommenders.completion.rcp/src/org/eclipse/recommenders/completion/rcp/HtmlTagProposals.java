/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.HTMLTagCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class HtmlTagProposals {

    public static Map<IJavaCompletionProposal, CompletionProposal> computeHtmlTagProposals(
            HTMLTagCompletionProposalComputer htmlTagProposalComputer, JavaContentAssistInvocationContext coreContext) {
        Map<IJavaCompletionProposal, CompletionProposal> result = Maps.newHashMap();
        htmlTagProposalComputer.sessionStarted();
        List<ICompletionProposal> htmlTagProposals = htmlTagProposalComputer.computeCompletionProposals(coreContext,
                new NullProgressMonitor());
        htmlTagProposalComputer.sessionEnded();
        for (ICompletionProposal htmlTagProposal : htmlTagProposals) {
            if (htmlTagProposal instanceof IJavaCompletionProposal) { // Should never be false
                result.put((IJavaCompletionProposal) htmlTagProposal, null);
            }
        }
        return result;
    }

}
