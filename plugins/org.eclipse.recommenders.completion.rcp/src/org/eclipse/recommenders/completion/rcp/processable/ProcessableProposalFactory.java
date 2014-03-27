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
package org.eclipse.recommenders.completion.rcp.processable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
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
import org.eclipse.jdt.internal.ui.text.java.LazyPackageCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.utils.Throws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Creates more flexible completion proposals from original proposals
 */
@SuppressWarnings("restriction")
public class ProcessableProposalFactory implements IProcessableProposalFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessableProposalFactory.class);
    private static Class<JavaMethodCompletionProposal> javaMethodCompletionProposalClass;
    private static Class<JavaFieldWithCastedReceiverCompletionProposal> javaFieldWithCastedReceiverCompletionProposalClass;
    private static Class<OverrideCompletionProposal> overrideCompletionProposalClass;
    private static Class<AnonymousTypeCompletionProposal> anonymousTypeCompletionProposalClass;
    private static Class<JavaCompletionProposal> javaCompletionProposalClass;
    private static Class<LazyGenericTypeProposal> lazyGenericTypeProposalClass;
    private static Class<LazyJavaTypeCompletionProposal> lazyJavaTypeCompletionProposalClass;
    private static Class<FilledArgumentNamesMethodProposal> filledArgumentNamesMethodProposalClass;
    private static Class<ParameterGuessingProposal> parameterGuessingProposalClass;
    private static Class<MethodDeclarationCompletionProposal> methodDeclarationCompletionProposalClass;
    private static Class<LazyPackageCompletionProposal> lazyPackageCompletionProposalClass;
    private static Class<GetterSetterCompletionProposal> getterSetterCompletionProposalClass;

    static {
        try {
            javaMethodCompletionProposalClass = JavaMethodCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            javaFieldWithCastedReceiverCompletionProposalClass = JavaFieldWithCastedReceiverCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            overrideCompletionProposalClass = OverrideCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            anonymousTypeCompletionProposalClass = AnonymousTypeCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            javaCompletionProposalClass = JavaCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            lazyGenericTypeProposalClass = LazyGenericTypeProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            lazyJavaTypeCompletionProposalClass = LazyJavaTypeCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            filledArgumentNamesMethodProposalClass = FilledArgumentNamesMethodProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            parameterGuessingProposalClass = ParameterGuessingProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            methodDeclarationCompletionProposalClass = MethodDeclarationCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            methodDeclarationCompletionProposalClass = MethodDeclarationCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            lazyPackageCompletionProposalClass = LazyPackageCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
        try {
            getterSetterCompletionProposalClass = GetterSetterCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            LOG.warn("Error while loading completion proposal class", e);
        }
    }

    public ProcessableProposalFactory() {
    }

    public static IJavaCompletionProposal create(CompletionProposal coreProposal, IJavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context, IProcessableProposalFactory factory) {

        final Class<? extends IJavaCompletionProposal> c = uiProposal.getClass();
        try {
            if (javaMethodCompletionProposalClass == c) {
                return factory.newJavaMethodCompletionProposal(coreProposal, context);
            } else if (javaFieldWithCastedReceiverCompletionProposalClass == c) {
                return factory.newJavaFieldWithCastedReceiverCompletionProposal(coreProposal,
                        (JavaFieldWithCastedReceiverCompletionProposal) uiProposal, context);
            } else if (overrideCompletionProposalClass == c) {
                return factory.newOverrideCompletionProposal(coreProposal, (OverrideCompletionProposal) uiProposal,
                        context);
            } else if (anonymousTypeCompletionProposalClass == c) {
                return factory.newAnonymousTypeCompletionProposal(coreProposal,
                        (AnonymousTypeCompletionProposal) uiProposal, context);
            } else if (javaCompletionProposalClass == c) {
                return factory.newJavaCompletionProposal(coreProposal, (JavaCompletionProposal) uiProposal, context);
            } else if (lazyGenericTypeProposalClass == c) {
                return factory.newLazyGenericTypeProposal(coreProposal, context);
            } else if (lazyJavaTypeCompletionProposalClass == c) {
                return factory.newLazyJavaTypeCompletionProposal(coreProposal, context);
            } else if (filledArgumentNamesMethodProposalClass == c) {
                return factory.newFilledArgumentNamesMethodProposal(coreProposal, context);
            } else if (parameterGuessingProposalClass == c) {
                return factory.newParameterGuessingProposal(coreProposal, context);
            } else if (methodDeclarationCompletionProposalClass == c) {
                return factory.newMethodDeclarationCompletionProposal(coreProposal,
                        (MethodDeclarationCompletionProposal) uiProposal, context);
            } else if (lazyPackageCompletionProposalClass == c) {
                return factory.newLazyPackageCompletionProposal(coreProposal,
                        (LazyPackageCompletionProposal) uiProposal, context);
            } else if (getterSetterCompletionProposalClass == c) {
                return factory.newGetterSetterCompletionProposal(coreProposal,
                        (GetterSetterCompletionProposal) uiProposal, context);
            }
            // return the fallback proposal
            LOG.warn("Unknown JDT proposal type '{}' ('{}'). Returning original proposal instead.", c, //$NON-NLS-1$
                    uiProposal.getDisplayString());
            return uiProposal;
        } catch (final Exception e) {
            LOG.warn("Wrapping JDT proposal '{}' ('{}') failed. Returning original proposal instead.", c, //$NON-NLS-1$
                    uiProposal.getDisplayString(), e);
            return uiProposal;
        }
    }

    @Override
    public IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyGenericTypeProposal(coreProposal, context));
    }

    protected IProcessableProposal postConstruct(IProcessableProposal res) {
        res.setProposalProcessorManager(new ProposalProcessorManager(res));
        return res;
    }

    @Override
    public IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableFilledArgumentNamesMethodProposal(coreProposal, context));
    }

    @Override
    public IProcessableProposal newParameterGuessingProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        final boolean fillBestGuess = shouldFillArgumentNames();
        return postConstruct(new ProcessableParameterGuessingProposal(coreProposal, context, fillBestGuess));
    }

    private boolean shouldFillArgumentNames() {
        try {
            final boolean res = PreferenceConstants.getPreferenceStore().getBoolean(
                    PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
            return res;
        } catch (final Exception e) {
        }
        return false;
    }

    @Override
    public IProcessableProposal newAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            AnonymousTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context)
            throws JavaModelException {
        return postConstruct(new ProcessableAnonymousTypeCompletionProposal(coreProposal, uiProposal, context));
    }

    @Override
    public IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(new ProcessableJavaFieldWithCastedReceiverCompletionProposal(coreProposal, uiProposal,
                    context));
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(new ProcessableJavaCompletionProposal(coreProposal, uiProposal, context));
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavaMethodCompletionProposal(coreProposal, context));
    }

    @Override
    public IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreProposal,
            JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyJavaTypeCompletionProposal(coreProposal, context));
    }

    @Override
    public IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableOverrideCompletionProposal(coreProposal, uiProposal, context));
    }

    @Override
    public IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IJavaElement enclosingElement = context.getCoreContext().getEnclosingElement();
            // may be a type or a source field:
            IType type = null;
            if (enclosingElement instanceof IType) {
                type = (IType) enclosingElement;
            } else if (enclosingElement instanceof IField) {
                type = ((IField) enclosingElement).getDeclaringType();
            } else if (enclosingElement instanceof IMethod) {
                type = ((IMethod) enclosingElement).getDeclaringType();
            }
            if (type != null) {
                return postConstruct(ProcessableMethodDeclarationCompletionProposal.newProposal(coreProposal, type,
                        uiProposal.getRelevance()));
            }
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
        throw Throws.throwIllegalArgumentException("No type found"); //$NON-NLS-1$
    }

    @Override
    public IJavaCompletionProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IField field = (IField) uiProposal.getJavaElement();
            return postConstruct(new ProcessableGetterSetterCompletionProposal(coreProposal, field, uiProposal
                    .getDisplayString().startsWith("get"), uiProposal.getRelevance())); //$NON-NLS-1$
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IJavaCompletionProposal newLazyPackageCompletionProposal(CompletionProposal coreProposal,
            LazyPackageCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyPackageCompletionProposal(coreProposal, context));
    }
}
