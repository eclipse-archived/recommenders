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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

@SuppressWarnings("restriction")
public class SubwordsCompletionProposalFactory {

    public static AbstractJavaCompletionProposal createFromJDTProposal(final SubwordsProposalContext subwordsContext) {
        final IJavaCompletionProposal jdtProposal = subwordsContext.getJdtProposal();

        if (jdtProposal instanceof JavaMethodCompletionProposal) {
            return SubwordsJavaMethodCompletionProposal.create(subwordsContext);
        } else if (jdtProposal instanceof JavaFieldWithCastedReceiverCompletionProposal) {
            return SubwordsFieldCastedCompletionProposal.create(subwordsContext);
        } else if (jdtProposal instanceof OverrideCompletionProposal) {
            return SubwordsOverrideCompletionProposal.create(subwordsContext);
        } else if (jdtProposal instanceof AnonymousTypeCompletionProposal) {
            try {
                return SubwordsAnonymousCompletionProposal.create(subwordsContext);
            } catch (final CoreException e) {
                throw new RuntimeException(e);
            }
        } else if (jdtProposal instanceof JavaCompletionProposal) {
            return SubwordsJavaCompletionProposal.create(subwordsContext);
        } else if (jdtProposal instanceof LazyJavaTypeCompletionProposal) {
            return SubwordsJavaTypeCompletionProposal.create(subwordsContext);
        } else {
            // XXX should we throw an exception here?
            return null;
        }
    }

}
