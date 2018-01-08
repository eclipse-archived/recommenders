/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.FilledArgumentNamesMethodProposal;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyPackageCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocInlineTagCompletionProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocLinkTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

@SuppressWarnings("restriction")
public interface IProcessableProposalFactory {

    IProcessableProposal newAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            AnonymousTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context)
                    throws JavaModelException;

    IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal, JavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreCompletion,
            FilledArgumentNamesMethodProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreCompletion,
            JavaMethodCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreCompletion,
            LazyGenericTypeProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreCompletion,
            LazyJavaTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newParameterGuessingProposal(CompletionProposal coreCompletion,
            ParameterGuessingProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyPackageCompletionProposal(CompletionProposal coreProposal,
            LazyPackageCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newJavadocLinkTypeCompletionProposal(CompletionProposal coreProposal,
            JavadocLinkTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newJavadocInlineTagCompletionProposal(CompletionProposal coreProposal,
            JavadocInlineTagCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyJavaCompletionProposal(CompletionProposal coreProposal,
            LazyJavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context);
}
