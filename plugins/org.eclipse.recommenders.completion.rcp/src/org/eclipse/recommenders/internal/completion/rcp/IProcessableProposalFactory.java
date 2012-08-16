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
package org.eclipse.recommenders.internal.completion.rcp;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;

public interface IProcessableProposalFactory {

    // TODO: let's see we get rid of the ui proposals soon
    IProcessableProposal newAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            AnonymousTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) throws JavaModelException;

    IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal, JavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreCompletion,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreCompletion,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreCompletion,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreCompletion,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newParameterGuessingProposal(CompletionProposal coreCompletion,
            JavaContentAssistInvocationContext context);

    IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context);

    IJavaCompletionProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context);
}
