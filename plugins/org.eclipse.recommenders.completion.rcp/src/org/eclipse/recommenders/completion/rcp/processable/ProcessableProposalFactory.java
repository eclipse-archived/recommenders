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

import static org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.CompletionProposal;
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
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Reflections;

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

    // Cannot use class literals like below, as LazyModuleCompletionProposal has only been introduced with Oxygen.1.
    @SuppressWarnings("unchecked")
    private static Class<? extends LazyJavaCompletionProposal> lazyModuleCompletionProposals = (Class<? extends LazyJavaCompletionProposal>) Reflections
            .loadClass(false, ProcessableProposalFactory.class.getClassLoader(),
                    "org.eclipse.jdt.internal.ui.text.java.LazyModuleCompletionProposal")
            .orNull();

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

        Class<? extends IJavaCompletionProposal> c = uiProposal.getClass();
        try {
            if (javaMethodCompletionProposalClass == c) {
                return factory.newJavaMethodCompletionProposal(coreProposal,
                        (JavaMethodCompletionProposal) uiProposal, context);
            } else if (javaFieldWithCastedReceiverCompletionProposalClass == c) {
                return factory.newJavaFieldWithCastedReceiverCompletionProposal(coreProposal,
                        (JavaFieldWithCastedReceiverCompletionProposal) uiProposal, context);
            } else if (overrideCompletionProposalClass == c) {
                return factory.newOverrideCompletionProposal(coreProposal,
                        (OverrideCompletionProposal) uiProposal, context);
            } else if (anonymousTypeCompletionProposalClass == c) {
                return factory.newAnonymousTypeCompletionProposal(coreProposal,
                        (AnonymousTypeCompletionProposal) uiProposal, context);
            } else if (javaCompletionProposalClass == c) {
                return factory.newJavaCompletionProposal(coreProposal,
                        (JavaCompletionProposal) uiProposal, context);
            } else if (lazyGenericTypeProposalClass == c) {
                return factory.newLazyGenericTypeProposal(coreProposal,
                        (LazyGenericTypeProposal) uiProposal, context);
            } else if (lazyJavaTypeCompletionProposalClass == c) {
                return factory.newLazyJavaTypeCompletionProposal(coreProposal,
                        (LazyJavaTypeCompletionProposal) uiProposal, context);
            } else if (filledArgumentNamesMethodProposalClass == c) {
                return factory.newFilledArgumentNamesMethodProposal(coreProposal,
                        (FilledArgumentNamesMethodProposal) uiProposal, context);
            } else if (parameterGuessingProposalClass == c) {
                return factory.newParameterGuessingProposal(coreProposal,
                        (ParameterGuessingProposal) uiProposal, context);
            } else if (methodDeclarationCompletionProposalClass == c) {
                return factory.newMethodDeclarationCompletionProposal(coreProposal,
                        (MethodDeclarationCompletionProposal) uiProposal, context);
            } else if (lazyPackageCompletionProposalClass == c) {
                return factory.newLazyPackageCompletionProposal(coreProposal,
                        (LazyPackageCompletionProposal) uiProposal, context);
            } else if (getterSetterCompletionProposalClass == c) {
                return factory.newGetterSetterCompletionProposal(coreProposal,
                        (GetterSetterCompletionProposal) uiProposal, context);
            } else if (javadocLinkTypeCompletionProposalClass == c) {
                return factory.newJavadocLinkTypeCompletionProposal(coreProposal,
                        (JavadocLinkTypeCompletionProposal) uiProposal, context);
            } else if (javadocInlineTagCompletionProposalClass == c) {
                return factory.newJavadocInlineTagCompletionProposal(coreProposal,
                        (JavadocInlineTagCompletionProposal) uiProposal, context);
            } else if (lazyJavaCompletionProposaClass == c || lazyModuleCompletionProposals == c) {
                return factory.newLazyJavaCompletionProposal(coreProposal,
                        (LazyJavaCompletionProposal) uiProposal, context);
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
        } catch (Exception e) {
            log(ERROR_FAILED_TO_WRAP_JDT_PROPOSAL, e, c, uiProposal.getDisplayString());
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

    @Override
    public IProcessableProposal newLazyGenericTypeProposal(CompletionProposal coreProposal,
            LazyGenericTypeProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableLazyGenericTypeProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newFilledArgumentNamesMethodProposal(CompletionProposal coreProposal,
            FilledArgumentNamesMethodProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableFilledArgumentNamesMethodProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newParameterGuessingProposal(CompletionProposal coreProposal,
            ParameterGuessingProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableParameterGuessingProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newAnonymousTypeCompletionProposal(CompletionProposal coreProposal,
            AnonymousTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context)
            throws JavaModelException {
        return postConstruct(
                ProcessableAnonymousTypeCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newJavaFieldWithCastedReceiverCompletionProposal(CompletionProposal coreProposal,
            JavaFieldWithCastedReceiverCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(ProcessableJavaFieldWithCastedReceiverCompletionProposal.toProcessableProposal(uiProposal,
                coreProposal, context), uiProposal);
    }

    @Override
    public IProcessableProposal newJavaCompletionProposal(CompletionProposal coreProposal,
            JavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(ProcessableJavaCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newJavadocLinkTypeCompletionProposal(CompletionProposal coreProposal,
            JavadocLinkTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableJavadocLinkTypeCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newJavadocInlineTagCompletionProposal(CompletionProposal coreProposal,
            JavadocInlineTagCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableJavadocInlineTagCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newJavaMethodCompletionProposal(CompletionProposal coreProposal,
            JavaMethodCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableJavaMethodCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newLazyJavaTypeCompletionProposal(CompletionProposal coreProposal,
            LazyJavaTypeCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableLazyJavaTypeCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newOverrideCompletionProposal(CompletionProposal coreProposal,
            OverrideCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableOverrideCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newMethodDeclarationCompletionProposal(CompletionProposal coreProposal,
            MethodDeclarationCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableMethodDeclarationCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newGetterSetterCompletionProposal(CompletionProposal coreProposal,
            GetterSetterCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableGetterSetterCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newLazyPackageCompletionProposal(CompletionProposal coreProposal,
            LazyPackageCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableLazyPackageCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
    }

    @Override
    public IProcessableProposal newLazyJavaCompletionProposal(CompletionProposal coreProposal,
            LazyJavaCompletionProposal uiProposal, JavaContentAssistInvocationContext context) {
        return postConstruct(
                ProcessableLazyJavaCompletionProposal.toProcessableProposal(uiProposal, coreProposal, context),
                uiProposal);
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

        copyProposalInfo(originalProposal, processableProposal);

        return processableProposal;
    }

    private static Method GET_PROPOSAL_INFO = Reflections
            .getDeclaredMethod(true, AbstractJavaCompletionProposal.class, "getProposalInfo").orNull(); //$NON-NLS-1$

    /**
     * Copies the proposal info from the original proposal to the processable proposal.
     * 
     * This is necessary as we cannot simply wrap the original {@link IJavaCompletionProposal} and delegate to
     * {@link AbstractJavaCompletionProposal#getProposalInfo} as that method is {@code protected} and hence not visible
     * to a wrapper.
     */
    private static void copyProposalInfo(IJavaCompletionProposal originalProposal,
            IProcessableProposal processableProposal) {
        // XXX this method should under no circumstances throw any exception
        if (Checks.anyIsNull(GET_PROPOSAL_INFO, processableProposal, originalProposal)) {
            return;
        }
        try {
            ProposalInfo info = (ProposalInfo) GET_PROPOSAL_INFO.invoke(originalProposal);
            processableProposal.setProposalInfo(info);
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_SET_PROPOSAL_INFO, e, processableProposal);
        }
    }
}
