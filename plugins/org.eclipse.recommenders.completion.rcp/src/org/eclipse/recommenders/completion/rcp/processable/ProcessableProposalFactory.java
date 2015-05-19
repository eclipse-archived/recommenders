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

import static org.apache.commons.lang3.StringUtils.startsWithAny;
import static org.eclipse.recommenders.internal.completion.rcp.LogMessages.ERROR_UNEXPECTED_PROPOSAL_KIND;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
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
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocInlineTagCompletionProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocLinkTypeCompletionProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Reflections;
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
    private static Class<LazyJavaCompletionProposal> lazyJavaCompletionProposaClass;
    private static Class<FilledArgumentNamesMethodProposal> filledArgumentNamesMethodProposalClass;
    private static Class<ParameterGuessingProposal> parameterGuessingProposalClass;
    private static Class<MethodDeclarationCompletionProposal> methodDeclarationCompletionProposalClass;
    private static Class<LazyPackageCompletionProposal> lazyPackageCompletionProposalClass;
    private static Class<GetterSetterCompletionProposal> getterSetterCompletionProposalClass;
    private static Class<JavadocLinkTypeCompletionProposal> javadocLinkTypeCompletionProposalClass;
    private static Class<JavadocInlineTagCompletionProposal> javadocInlineTagCompletionProposalClass;

    private static Method proposalInfoMethod = Reflections
            .getDeclaredMethod(AbstractJavaCompletionProposal.class, "getProposalInfo").orNull(); //$NON-NLS-1$

    static {
        // No all versions of JDT offer all kinds of CompletionProposal. Probe using reflection.
        try {
            javaMethodCompletionProposalClass = JavaMethodCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            javaFieldWithCastedReceiverCompletionProposalClass = JavaFieldWithCastedReceiverCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            overrideCompletionProposalClass = OverrideCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            anonymousTypeCompletionProposalClass = AnonymousTypeCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            javaCompletionProposalClass = JavaCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            lazyGenericTypeProposalClass = LazyGenericTypeProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            lazyJavaCompletionProposaClass = LazyJavaCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            lazyJavaTypeCompletionProposalClass = LazyJavaTypeCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            filledArgumentNamesMethodProposalClass = FilledArgumentNamesMethodProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            parameterGuessingProposalClass = ParameterGuessingProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            methodDeclarationCompletionProposalClass = MethodDeclarationCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            methodDeclarationCompletionProposalClass = MethodDeclarationCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            lazyPackageCompletionProposalClass = LazyPackageCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            getterSetterCompletionProposalClass = GetterSetterCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            javadocLinkTypeCompletionProposalClass = JavadocLinkTypeCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
        try {
            javadocInlineTagCompletionProposalClass = JavadocInlineTagCompletionProposal.class;
        } catch (NoClassDefFoundError e) {
            logWarning(e);
        }
    }

    private static void logWarning(NoClassDefFoundError e) {
        LOG.warn("Error while loading completion proposal class", e); //$NON-NLS-1$
    }

    public ProcessableProposalFactory() {
    }

    public static IJavaCompletionProposal create(CompletionProposal coreProposal, IJavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context, IProcessableProposalFactory factory) {

        final Class<? extends IJavaCompletionProposal> c = uiProposal.getClass();
        // TODO the handling of setProposalInfo should be improved soon.
        try {
            if (javaMethodCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavaMethodCompletionProposal(coreProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (javaFieldWithCastedReceiverCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavaFieldWithCastedReceiverCompletionProposal(coreProposal,
                        (JavaFieldWithCastedReceiverCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (overrideCompletionProposalClass == c) {
                IProcessableProposal res = factory.newOverrideCompletionProposal(coreProposal,
                        (OverrideCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (anonymousTypeCompletionProposalClass == c) {
                IProcessableProposal res = factory.newAnonymousTypeCompletionProposal(coreProposal,
                        (AnonymousTypeCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (javaCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavaCompletionProposal(coreProposal,
                        (JavaCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (lazyGenericTypeProposalClass == c) {
                IProcessableProposal res = factory.newLazyGenericTypeProposal(coreProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (lazyJavaTypeCompletionProposalClass == c) {
                IProcessableProposal res = factory.newLazyJavaTypeCompletionProposal(coreProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (filledArgumentNamesMethodProposalClass == c) {
                IProcessableProposal res = factory.newFilledArgumentNamesMethodProposal(coreProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (parameterGuessingProposalClass == c) {
                IProcessableProposal res = factory.newParameterGuessingProposal(coreProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (methodDeclarationCompletionProposalClass == c) {
                IProcessableProposal res = factory.newMethodDeclarationCompletionProposal(coreProposal,
                        (MethodDeclarationCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (lazyPackageCompletionProposalClass == c) {
                IProcessableProposal res = factory.newLazyPackageCompletionProposal(coreProposal,
                        (LazyPackageCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (getterSetterCompletionProposalClass == c) {
                IProcessableProposal res = factory.newGetterSetterCompletionProposal(coreProposal,
                        (GetterSetterCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (javadocLinkTypeCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavadocLinkTypeCompletionProposal(coreProposal,
                        (JavadocLinkTypeCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (javadocInlineTagCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavadocInlineTagCompletionProposal(coreProposal,
                        (JavadocInlineTagCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (lazyJavaCompletionProposaClass == c) {
                IProcessableProposal res = factory.newLazyJavaCompletionProposa(coreProposal,
                        (LazyJavaCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            }

            // log error and return the fallback proposal
            log(ERROR_UNEXPECTED_PROPOSAL_KIND, c, uiProposal.getDisplayString());
            return uiProposal;
        } catch (final Exception e) {
            LOG.warn("Wrapping JDT proposal '{}' ('{}') failed. Returning original proposal instead.", c, //$NON-NLS-1$
                    uiProposal.getDisplayString(), e);
            return uiProposal;
        }
    }

    private static void setProposalInfo(IProcessableProposal crProposal, IJavaCompletionProposal uiProposal) {
        // XXX this method should under no circumstances throw any exception
        if (Checks.anyIsNull(proposalInfoMethod, crProposal, uiProposal)) {
            return;
        }
        try {
            ProposalInfo info = (ProposalInfo) proposalInfoMethod.invoke(uiProposal);
            crProposal.setProposalInfo(info);
        } catch (Exception e) {
            LOG.warn("Failed to set proposal info to '{}'). Returning proposal without additional info.", crProposal); //$NON-NLS-1$
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
            final boolean res = PreferenceConstants.getPreferenceStore()
                    .getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
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
            return postConstruct(
                    new ProcessableJavaFieldWithCastedReceiverCompletionProposal(coreProposal, uiProposal, context));
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
    public IProcessableProposal newJavadocLinkTypeCompletionProposal(CompletionProposal coreProposal,
            JavadocLinkTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavadocLinkTypeCompletionProposal(coreProposal, context));
    }

    @Override
    public IProcessableProposal newJavadocInlineTagCompletionProposal(CompletionProposal coreProposal,
            JavadocInlineTagCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavadocInlineTagCompletionProposal(coreProposal, context));
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
    public IProcessableProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IField field = (IField) uiProposal.getJavaElement();
            return postConstruct(new ProcessableGetterSetterCompletionProposal(coreProposal, field,
                    startsWithAny(uiProposal.getDisplayString(), "get", "is"), uiProposal.getRelevance())); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newLazyPackageCompletionProposal(CompletionProposal coreProposal,
            LazyPackageCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyPackageCompletionProposal(coreProposal, context));
    }

    @Override
    public IProcessableProposal newLazyJavaCompletionProposa(CompletionProposal coreProposal,
            LazyJavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyJavaCompletionProposal(coreProposal, context));
    }
}
