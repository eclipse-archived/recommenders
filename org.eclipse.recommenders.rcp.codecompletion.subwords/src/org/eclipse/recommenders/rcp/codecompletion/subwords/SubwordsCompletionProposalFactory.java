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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class SubwordsCompletionProposalFactory {

    public static AbstractJavaCompletionProposal createFromJDTProposal(final IJavaCompletionProposal jdtProposal,
            final CompletionProposal initialProposal, final JavaContentAssistInvocationContext ctx) {
        if (jdtProposal instanceof JavaMethodCompletionProposal) {
            return new SubwordsJavaMethodCompletionProposal(initialProposal, ctx);
        } else if (jdtProposal instanceof JavaCompletionProposal) {
            return new SubwordsJavaCompletionProposal((JavaCompletionProposal) jdtProposal, initialProposal, ctx);
        } else if (jdtProposal instanceof LazyJavaTypeCompletionProposal) {
            return new SubwordsJavaTypeCompletionProposal(initialProposal, ctx);
        } else if (jdtProposal instanceof JavaFieldWithCastedReceiverCompletionProposal) {
            return new SubwordsFieldCastedCompletionProposal(
                    (JavaFieldWithCastedReceiverCompletionProposal) jdtProposal, initialProposal, ctx);
        } else if (jdtProposal instanceof OverrideCompletionProposal) {
            return new SubwordsOverrideCompletionProposal((OverrideCompletionProposal) jdtProposal, initialProposal,
                    ctx);
        } else if (jdtProposal instanceof AnonymousTypeCompletionProposal) {
            try {
                return new SubwordsAnonymousCompletionProposal((AnonymousTypeCompletionProposal) jdtProposal,
                        initialProposal, ctx);
            } catch (final CoreException e) {
                throw new RuntimeException(e);
            }
        } else {
            // XXX should we throw an exception here?
            return null;
        }
    }

}
