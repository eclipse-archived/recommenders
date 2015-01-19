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
package org.eclipse.recommenders.internal.chain.rcp;

import static org.eclipse.recommenders.completion.rcp.CompletionContexts.toContentAssistInvocationContext;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.internal.chain.rcp.ChainRcpModule.ChainCompletion;

import com.google.common.collect.Iterables;

public class ChainQuickAssistProcessor implements IQuickAssistProcessor {

    private final ChainCompletionProposalComputer computer;

    private final IPreferenceStore preferenceStore;

    @Inject
    public ChainQuickAssistProcessor(ChainCompletionProposalComputer computer,
            @ChainCompletion final IPreferenceStore preferenceStore) {
        this.computer = computer;
        this.preferenceStore = preferenceStore;
    }

    @Override
    public boolean hasAssists(IInvocationContext context) throws CoreException {
        return false;
    }

    @Override
    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
            throws CoreException {
        if (preferenceStore.getBoolean(ChainsPreferencePage.PREF_ENABLE_QUICK_ASSIST_CHAINS)) {
            JavaContentAssistInvocationContext ctx = toContentAssistInvocationContext(context);
            List<IJavaCompletionProposal> proposals = cast(computer.computeCompletionProposals(ctx,
                    new NullProgressMonitor()));
            return Iterables.toArray(proposals, IJavaCompletionProposal.class);
        } else {
            return new IJavaCompletionProposal[0];
        }
    }
}
