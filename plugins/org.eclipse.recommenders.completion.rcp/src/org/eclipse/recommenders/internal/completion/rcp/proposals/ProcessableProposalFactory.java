/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.internal.completion.rcp.proposals;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.FilledArgumentNamesMethodProposal;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;
import org.eclipse.recommenders.internal.completion.rcp.IProcessableProposalFactory;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Throwables;

/**
 * Creates more flexible completion proposals from original proposals
 */
public class ProcessableProposalFactory implements IProcessableProposalFactory {

    @Inject
    public ProcessableProposalFactory() {
    }

    public static IJavaCompletionProposal create(CompletionProposal coreProposal, IJavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context, IProcessableProposalFactory factory) {

        final Class<? extends IJavaCompletionProposal> c = uiProposal.getClass();

        try {
            if (JavaMethodCompletionProposal.class == c) {
                return factory.newJavaMethodCompletionProposal(coreProposal, context);
            } else if (JavaFieldWithCastedReceiverCompletionProposal.class == c) {
                return factory.newJavaFieldWithCastedReceiverCompletionProposal(coreProposal,
                        (JavaFieldWithCastedReceiverCompletionProposal) uiProposal,
                        context);
            } else if (OverrideCompletionProposal.class == c) {
                return factory.newOverrideCompletionProposal(coreProposal,
                        (OverrideCompletionProposal) uiProposal,
                        context);
            } else if (AnonymousTypeCompletionProposal.class == c) {
                return factory.newAnonymousTypeCompletionProposal(coreProposal,
                        (AnonymousTypeCompletionProposal) uiProposal,
                        context);
            } else if (JavaCompletionProposal.class == c) {
                return factory.newJavaCompletionProposal(coreProposal, (JavaCompletionProposal) uiProposal, context);
            } else if (LazyGenericTypeProposal.class == c) {
                return factory.newLazyGenericTypeProposal(coreProposal, context);
            } else if (LazyJavaTypeCompletionProposal.class == c) {
                return factory.newLazyJavaTypeCompletionProposal(coreProposal, context);
            } else if (FilledArgumentNamesMethodProposal.class == c) {
                return factory.newFilledArgumentNamesMethodProposal(coreProposal, context);
            } else if (ParameterGuessingProposal.class == c) {
                return factory.newParameterGuessingProposal(coreProposal, context);
            } else if (MethodDeclarationCompletionProposal.class == c) {
                return factory.newMethodDeclarationCompletionProposal(coreProposal,
                        (MethodDeclarationCompletionProposal) uiProposal,
                        context);
            } else if (GetterSetterCompletionProposal.class == c) {
                return factory.newGetterSetterCompletionProposal(coreProposal,
                        (GetterSetterCompletionProposal) uiProposal,
                        context);
            }
            // return the fallback proposal
            RecommendersUtilsPlugin
                    .logWarning("Unknown JDT proposal type '%s' ('%s'). Returning original proposal instead.",
                            c,
                            uiProposal.getDisplayString());
            return uiProposal;
        } catch (final Exception e) {
            RecommendersUtilsPlugin.logWarning(e,
                    "Wrapping JDT proposal '%s' ('%s') failed. Returning original proposal instead.",
                    c,
                    uiProposal.getDisplayString());
            return uiProposal;
        }
    }

    public IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyGenericTypeProposal(coreProposal, context));
    }

    protected IProcessableProposal postConstruct(IProcessableProposal res) {
        res.setProposalProcessorManager(new ProposalProcessorManager(res));
        return res;
    }

    public IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableFilledArgumentNamesMethodProposal(coreProposal, context));
    }

    public IProcessableProposal newParameterGuessingProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        final boolean fillBestGuess = shouldFillArgumentNames();
        return postConstruct(new ProcessableParameterGuessingProposal(coreProposal, context, fillBestGuess));
    }

    private boolean shouldFillArgumentNames() {
        try {
            final boolean res =
                    PreferenceConstants.getPreferenceStore()
                            .getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
            return res;
        } catch (final Exception e) {
        }
        return false;
    }

    public IProcessableProposal newAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            AnonymousTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) throws JavaModelException {
        return postConstruct(new ProcessableAnonymousTypeCompletionProposal(coreProposal, uiProposal, context));
    }

    public IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(new ProcessableJavaFieldWithCastedReceiverCompletionProposal(coreProposal, uiProposal,
                    context));
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    public IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(new ProcessableJavaCompletionProposal(coreProposal, uiProposal, context));
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    public IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavaMethodCompletionProposal(coreProposal, context));
    }

    public IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyJavaTypeCompletionProposal(coreProposal, context));
    }

    public IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableOverrideCompletionProposal(coreProposal, uiProposal, context));
    }

    @Override
    public IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {

            IType type = (IType) context.getCoreContext().getEnclosingElement();
            if (type != null)
                return postConstruct(ProcessableMethodDeclarationCompletionProposal.newProposal(coreProposal,
                        type,
                        uiProposal.getRelevance()));
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
        throw Throws.throwIllegalArgumentException("no type found");
    }

    @Override
    public IJavaCompletionProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IField field = (IField) uiProposal.getJavaElement();
            return postConstruct(new ProcessableGetterSetterCompletionProposal(coreProposal, field, uiProposal
                    .getDisplayString().startsWith("get"), uiProposal.getRelevance()));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
