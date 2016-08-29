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
import static org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages.ERROR_UNEXPECTED_PROPOSAL_KIND;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
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
import org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;
import org.eclipse.recommenders.utils.Throws;

import com.google.common.base.Throwables;

/**
 * Creates more flexible completion proposals from original proposals
 */
@SuppressWarnings("restriction")
public class ProcessableProposalFactory implements IProcessableProposalFactory {

    private static final String ORG_ECLIPSE_OBJECTTEAMS_OTDT = "org.eclipse.objectteams.otdt";

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
            .getDeclaredMethod(true, AbstractJavaCompletionProposal.class, "getProposalInfo").orNull(); //$NON-NLS-1$

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
        Logs.log(LogMessages.ERROR_FAILED_TO_LOAD_COMPLETION_PROPOSAL_CLASS, e);
    }

    public ProcessableProposalFactory() {
    }

    public static IJavaCompletionProposal create(CompletionProposal coreProposal, IJavaCompletionProposal uiProposal,
            JavaContentAssistInvocationContext context, IProcessableProposalFactory factory) {

        final Class<? extends IJavaCompletionProposal> c = uiProposal.getClass();
        // TODO the handling of setProposalInfo should be improved soon.
        try {
            if (javaMethodCompletionProposalClass == c) {
                IProcessableProposal res = factory.newJavaMethodCompletionProposal(coreProposal,
                        (JavaMethodCompletionProposal) uiProposal, context);
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
                IProcessableProposal res = factory.newLazyGenericTypeProposal(coreProposal,
                        (LazyGenericTypeProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (lazyJavaTypeCompletionProposalClass == c) {
                IProcessableProposal res = factory.newLazyJavaTypeCompletionProposal(coreProposal,
                        (LazyJavaTypeCompletionProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (filledArgumentNamesMethodProposalClass == c) {
                IProcessableProposal res = factory.newFilledArgumentNamesMethodProposal(coreProposal,
                        (FilledArgumentNamesMethodProposal) uiProposal, context);
                setProposalInfo(res, uiProposal);
                return res;
            } else if (parameterGuessingProposalClass == c) {
                IProcessableProposal res = factory.newParameterGuessingProposal(coreProposal,
                        (ParameterGuessingProposal) uiProposal, context);
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

            // Some plug-ins are known to add their own proposals to JDT's Java editor.
            // While we cannot make arbitrary proposals processable, this is likely to be fine and we should not
            // complain about such proposals.

            // See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=497180>
            if (isWhitelisted(uiProposal, ORG_ECLIPSE_OBJECTTEAMS_OTDT)) {
                return uiProposal;
            }

            // log error and return the fallback proposal
            log(ERROR_UNEXPECTED_PROPOSAL_KIND, c, uiProposal.getDisplayString());
            return uiProposal;
        } catch (final Exception e) {
            log(LogMessages.ERROR_FAILED_TO_WRAP_JDT_PROPOSAL, e, c, uiProposal.getDisplayString());
            return uiProposal;
        }
    }

    private static boolean isWhitelisted(IJavaCompletionProposal uiProposal, String whitelistedPackage) {
        String uiProposalPackage = uiProposal.getClass().getPackage().getName();
        if (uiProposalPackage.startsWith(whitelistedPackage)) {
            if (uiProposalPackage.length() == whitelistedPackage.length()) {
                return true; // in whitelisted package
            } else if (uiProposalPackage.charAt(whitelistedPackage.length()) == '.') {
                return true; // in subpackage of whitelisted package
            } else {
                return false;
            }
        } else {
            return false;
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
            Logs.log(LogMessages.ERROR_FAILED_TO_SET_PROPOSAL_INFO, e, crProposal);
        }
    }

    @Override
    public IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreProposal,
            LazyGenericTypeProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyGenericTypeProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreProposal,
            FilledArgumentNamesMethodProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableFilledArgumentNamesMethodProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newParameterGuessingProposal(CompletionProposal coreProposal,
            ParameterGuessingProposal uiProposal, JavaContentAssistInvocationContext context) {
        final boolean fillBestGuess = shouldFillArgumentNames();
        return postConstruct(new ProcessableParameterGuessingProposal(coreProposal, context, fillBestGuess),
                uiProposal);
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
        return postConstruct(new ProcessableAnonymousTypeCompletionProposal(coreProposal, uiProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(
                    new ProcessableJavaFieldWithCastedReceiverCompletionProposal(coreProposal, uiProposal, context),
                    uiProposal);
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            return postConstruct(new ProcessableJavaCompletionProposal(coreProposal, uiProposal, context), uiProposal);
        } catch (JavaModelException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newJavadocLinkTypeCompletionProposal(CompletionProposal coreProposal,
            JavadocLinkTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavadocLinkTypeCompletionProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newJavadocInlineTagCompletionProposal(CompletionProposal coreProposal,
            JavadocInlineTagCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavadocInlineTagCompletionProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreProposal,
            JavaMethodCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableJavaMethodCompletionProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreProposal,
            LazyJavaTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyJavaTypeCompletionProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableOverrideCompletionProposal(coreProposal, uiProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IJavaElement enclosingElement = context.getCoreContext().getEnclosingElement();
            IType type = null;
            if (enclosingElement instanceof IType) {
                type = (IType) enclosingElement;
            } else if (enclosingElement instanceof IMember) {
                type = ((IMember) enclosingElement).getDeclaringType();
            }
            if (type == null) {
                throw Throws.throwIllegalArgumentException("No type found for enclosing element %s", enclosingElement); //$NON-NLS-1$
            }
            return postConstruct(ProcessableMethodDeclarationCompletionProposal.newProposal(coreProposal, type,
                    uiProposal.getRelevance()), uiProposal);
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        try {
            IField field = (IField) uiProposal.getJavaElement();
            return postConstruct(
                    new ProcessableGetterSetterCompletionProposal(coreProposal, field,
                            startsWithAny(uiProposal.getDisplayString(), "get", "is"), uiProposal.getRelevance()), //$NON-NLS-1$ //$NON-NLS-2$
                    uiProposal);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IProcessableProposal newLazyPackageCompletionProposal(CompletionProposal coreProposal,
            LazyPackageCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyPackageCompletionProposal(coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newLazyJavaCompletionProposa(CompletionProposal coreProposal,
            LazyJavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(new ProcessableLazyJavaCompletionProposal(coreProposal, context), uiProposal);
    }

    /**
     * {@link AbstractJavaCompletionProposal#setTriggerCharacters(char[])} is called by
     * {@link org.eclipse.jdt.ui.text.java.CompletionProposalCollector} after a new completion proposal is created. We
     * must copy the trigger characters from the originalProposal to the processableProposal in order to mimic the JDT
     * behavior.
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=471386">Bug 471386</a>
     */
    protected <T extends AbstractJavaCompletionProposal & IProcessableProposal> T postConstruct(T processableProposal,
            AbstractJavaCompletionProposal originalProposal) {
        processableProposal.setProposalProcessorManager(new ProposalProcessorManager(processableProposal));
        processableProposal.setTriggerCharacters(originalProposal.getTriggerCharacters());
        return processableProposal;
    }
}
