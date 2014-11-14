/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp.model;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.recommenders.internal.stacktraces.rcp.Constants;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.VisitorImpl;
import org.eclipse.recommenders.utils.AnonymousId;
import org.eclipse.recommenders.utils.gson.EmfFieldExclusionStrategy;
import org.eclipse.recommenders.utils.gson.UuidTypeAdapter;
import org.osgi.framework.Bundle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ErrorReports {

    public static final class ClearMessagesVisitor extends VisitorImpl {
        @Override
        public void visit(Status status) {
            status.setMessage(Constants.HIDDEN);
        }

        @Override
        public void visit(Throwable throwable) {
            throwable.setMessage(Constants.HIDDEN);
        }
    }

    public static final class AnonymizeStacktraceVisitor extends VisitorImpl {
        private List<String> whitelist;

        public AnonymizeStacktraceVisitor(List<String> whitelist) {
            this.whitelist = whitelist;
        }

        @Override
        public void visit(Throwable throwable) {
            if (!isWhitelisted(throwable.getClassName(), whitelist)) {
                throwable.setClassName(Constants.HIDDEN);
            }
        }

        @Override
        public void visit(StackTraceElement element) {
            if (!isWhitelisted(element.getClassName(), whitelist)) {
                element.setClassName(Constants.HIDDEN);
                element.setMethodName(Constants.HIDDEN);
                element.setFileName(Constants.HIDDEN);
                element.setLineNumber(-1);
            }
        }
    }

    public static final class ThrowableFingerprintComputer extends VisitorImpl {

        private StringBuilder content = new StringBuilder();
        private List<String> whitelist;
        private int maxframes;

        public ThrowableFingerprintComputer(List<String> whitelist, int maxframes) {
            this.whitelist = whitelist;
            this.maxframes = maxframes;
        }

        @Override
        public void visit(StackTraceElement element) {
            if (maxframes < 0) {
                return;
            }
            maxframes--;
            if (isWhitelisted(element.getClassName(), whitelist)) {
                content.append(element.getClassName()).append(element.getMethodName());
            }
        }

        @Override
        public void visit(Throwable throwable) {
            if (isWhitelisted(throwable.getClassName(), whitelist)) {
                content.append(throwable.getClassName());
            }
        }

        public String hash() {
            return Hashing.murmur3_32().hashUnencodedChars(content.toString()).toString();
        }
    }

    public static final class CollectStackTraceElementPackagesVisitor extends VisitorImpl {
        public TreeSet<String> packages = Sets.newTreeSet();

        @Override
        public void visit(StackTraceElement element) {
            String pkg = replace(substringBeforeLast(element.getClassName(), "."), ".internal.", ".");
            packages.add(pkg);
        }
    }

    public static class PrettyPrintVisitor extends VisitorImpl {
        private static final int RIGHT_PADDING = 20;
        private StringBuilder reportStringBuilder = new StringBuilder();
        private StringBuilder statusStringBuilder = new StringBuilder();
        private StringBuilder bundlesStringBuilder = new StringBuilder();
        private StringBuilder stacktraceStringBuilder = new StringBuilder();

        private boolean firstThrowable = true;
        private boolean firstBundle = true;

        private void appendAttributes(EObject object, StringBuilder builder) {
            for (EAttribute attribute : object.eClass().getEAllAttributes()) {
                String line = String.format("%-" + RIGHT_PADDING + "s", attribute.getName() + ":")
                        + object.eGet(attribute) + "\n";
                builder.append(line);
            }
            builder.append("\n");
        }

        private void appendHeadline(String headline, StringBuilder builder) {
            String line = headline.replaceAll(".", "-") + "\n";
            builder.append(line);
            builder.append(headline + "\n");
            builder.append(line);
        }

        @Override
        public void visit(ErrorReport report) {
            appendHeadline("REPORT", reportStringBuilder);
            appendAttributes(report, reportStringBuilder);
            super.visit(report);
        }

        @Override
        public void visit(Status status) {
            appendHeadline("STATUS", statusStringBuilder);
            appendAttributes(status, statusStringBuilder);
            super.visit(status);
        }

        @Override
        public void visit(org.eclipse.recommenders.internal.stacktraces.rcp.model.Bundle bundle) {
            if (firstBundle) {
                appendHeadline("BUNDLES", bundlesStringBuilder);
                firstBundle = false;
            }
            appendAttributes(bundle, bundlesStringBuilder);
            super.visit(bundle);
        }

        @Override
        public void visit(Throwable throwable) {
            if (firstThrowable) {
                appendHeadline("STACKTRACE", stacktraceStringBuilder);
                firstThrowable = false;
            } else {
                stacktraceStringBuilder.append("Caused by: ");
            }
            stacktraceStringBuilder.append(throwable.getClassName() + ": " + throwable.getMessage() + "\n");
            super.visit(throwable);
        }

        @Override
        public void visit(StackTraceElement element) {
            stacktraceStringBuilder.append("\t at " + element.getClassName() + "." + element.getMethodName() + "("
                    + element.getFileName() + ":" + element.getLineNumber() + ")\n");
            super.visit(element);
        }

        public String print() {
            return new StringBuilder().append(statusStringBuilder).append(stacktraceStringBuilder)
                    .append(reportStringBuilder).append(bundlesStringBuilder).toString();
        }

    }

    public static class StatusFilterSetting {

        public StatusFilterSetting(String pluginPrefix, String... stacktraceClassPrefixes) {
            this.pluginPrefix = pluginPrefix;
            this.stacktraceClassPrefixes = Lists.newArrayList(stacktraceClassPrefixes);
        }

        final String pluginPrefix;
        final List<String> stacktraceClassPrefixes;
    }

    private static ModelFactory factory = ModelFactory.eINSTANCE;

    static boolean isWhitelisted(String className, List<String> whitelist) {
        for (String whiteListedPrefix : whitelist) {
            if (className.startsWith(whiteListedPrefix)) {
                return true;
            }
        }
        return false;
    }

    public static ErrorReport copy(ErrorReport org) {
        return EcoreUtil.copy(org);
    }

    public static String toJson(ErrorReport report, Settings settings, boolean pretty) {
        // work on a copy:
        report = copy(report);

        report.setName(settings.getName());
        report.setEmail(settings.getEmail());
        if (settings.isAnonymizeStrackTraceElements()) {
            anonymizeStackTrace(report, settings);
        }
        if (settings.isAnonymizeMessages()) {
            clearMessages(report);
        }

        Gson gson = createGson(pretty);
        String json = gson.toJson(report);
        return json;
    }

    private static Gson createGson(boolean pretty) {
        GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        builder.registerTypeAdapter(UUID.class, new UuidTypeAdapter());
        builder.addSerializationExclusionStrategy(new EmfFieldExclusionStrategy());
        if (pretty) {
            builder.setPrettyPrinting();
        }
        Gson gson = builder.create();
        return gson;
    }

    public static ErrorReport newErrorReport(IStatus event, Settings settings) {
        ErrorReport mReport = factory.createErrorReport();
        mReport.setAnonymousId(AnonymousId.getId());
        mReport.setEventId(UUID.randomUUID());
        mReport.setName(settings.getName());
        mReport.setEmail(settings.getEmail());

        mReport.setJavaRuntimeVersion(SystemUtils.JAVA_RUNTIME_VERSION);
        mReport.setEclipseBuildId(System.getProperty("eclipse.buildId", "-"));
        mReport.setEclipseProduct(System.getProperty("eclipse.product", "-"));
        mReport.setOsgiArch(System.getProperty("osgi.arch", "-"));
        mReport.setOsgiWs(System.getProperty("osgi.ws", "-"));
        mReport.setOsgiOs(System.getProperty(org.osgi.framework.Constants.FRAMEWORK_OS_NAME, "-"));
        mReport.setOsgiOsVersion(System.getProperty(org.osgi.framework.Constants.FRAMEWORK_OS_VERSION, "-"));
        mReport.setStatus(newStatus(event, settings));

        guessInvolvedPlugins(mReport);
        return mReport;
    }

    private static void guessInvolvedPlugins(ErrorReport mReport) {
        CollectStackTraceElementPackagesVisitor v = new CollectStackTraceElementPackagesVisitor();
        mReport.accept(v);
        Set<String> unique = Sets.newHashSet();
        for (String pkg : v.packages) {
            while (pkg.contains(".")) {
                Bundle guess = Platform.getBundle(pkg);
                pkg = StringUtils.substringBeforeLast(pkg, ".");
                if (guess != null) {
                    if (unique.add(guess.getSymbolicName())) {
                        org.eclipse.recommenders.internal.stacktraces.rcp.model.Bundle mBundle = factory.createBundle();
                        mBundle.setName(guess.getSymbolicName());
                        mBundle.setVersion(guess.getVersion().toString());
                        mReport.getPresentBundles().add(mBundle);
                    }
                    continue;
                }
            }
        }
    }

    @VisibleForTesting
    static Status newStatus(IStatus status, Settings settings) {
        Status mStatus = factory.createStatus();
        mStatus.setMessage(removeSourceFileContents(status.getMessage()));
        mStatus.setSeverity(status.getSeverity());
        mStatus.setCode(status.getCode());
        mStatus.setPluginId(status.getPlugin());

        Bundle bundle = Platform.getBundle(status.getPlugin());
        if (bundle != null) {
            mStatus.setPluginVersion(bundle.getVersion().toString());
        }

        List<Status> mChildren = mStatus.getChildren();
        if (status.getException() instanceof CoreException) {
            CoreException coreException = (CoreException) status.getException();
            IStatus coreExceptionStatus = coreException.getStatus();
            if (coreExceptionStatus != null) {
                mChildren.add(newStatus(coreExceptionStatus, settings));
            }
        }
        // some stacktraces from ui.monitoring should be filtered
        boolean needFiltering = "org.eclipse.ui.monitoring".equals(status.getPlugin())
                && (status.getCode() == 0 || status.getCode() == 1);
        // Multistatus handling
        for (IStatus child : status.getChildren()) {
            if (!(needFiltering && isFiltered(child))) {
                mChildren.add(newStatus(child, settings));
            }
        }

        if (status.getException() != null) {
            Throwable mException = newThrowable(status.getException());
            mStatus.setException(mException);
        }

        ThrowableFingerprintComputer fingerprint = new ThrowableFingerprintComputer(settings.getWhitelistedPackages(),
                1024);
        mStatus.accept(fingerprint);
        mStatus.setFingerprint(fingerprint.hash());

        return mStatus;
    }

    private static boolean isFiltered(IStatus status) {
        for (StatusFilterSetting filterSetting : Constants.MULTISTATUS_CHILD_STACKTRACES_FILTER_SETTINGS) {
            if (status.getPlugin().startsWith(filterSetting.pluginPrefix)
                    && isStacktraceFiltered(status, filterSetting.stacktraceClassPrefixes)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStacktraceFiltered(IStatus status, List<String> stacktraceClassPrefixes) {
        java.lang.StackTraceElement[] stackTrace = status.getException().getStackTrace();
        if (stackTrace.length != stacktraceClassPrefixes.size()) {
            return false;
        }
        for (int i = 0; i < stackTrace.length; i++) {
            if (!stackTrace[i].getClassName().startsWith(stacktraceClassPrefixes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static String removeSourceFileContents(String message) {
        if (message.contains(Constants.SOURCE_BEGIN_MESSAGE)) {
            return Constants.SOURCE_FILE_REMOVED;
        } else {
            return message;
        }
    }

    private static Throwable newThrowable(java.lang.Throwable throwable) {
        Throwable mThrowable = factory.createThrowable();
        mThrowable.setMessage(throwable.getMessage());
        mThrowable.setClassName(throwable.getClass().getName());
        List<StackTraceElement> mStackTrace = mThrowable.getStackTrace();
        for (java.lang.StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            StackTraceElement mStackTraceElement = factory.createStackTraceElement();
            mStackTraceElement.setFileName(stackTraceElement.getFileName());
            mStackTraceElement.setClassName(stackTraceElement.getClassName());
            mStackTraceElement.setMethodName(stackTraceElement.getMethodName());
            mStackTraceElement.setLineNumber(stackTraceElement.getLineNumber());
            mStackTrace.add(mStackTraceElement);
        }
        java.lang.Throwable cause = throwable.getCause();
        if (cause != null) {
            if (cause == throwable) {
                System.out.println("err");
            }
            mThrowable.setCause(newThrowable(cause));
        }
        return mThrowable;
    }

    public static void clearMessages(ErrorReport event) {
        event.accept(new ClearMessagesVisitor());
    }

    public static void anonymizeStackTrace(ErrorReport report, final Settings settings) {
        report.accept(new AnonymizeStacktraceVisitor(settings.getWhitelistedPackages()));
    }

    public static String prettyPrint(ErrorReport report, Settings settings) {
        if (settings.isAnonymizeStrackTraceElements()) {
            anonymizeStackTrace(report, settings);
        }
        if (settings.isAnonymizeMessages()) {
            clearMessages(report);
        }
        PrettyPrintVisitor prettyPrintVisitor = new PrettyPrintVisitor();
        report.accept(prettyPrintVisitor);
        return prettyPrintVisitor.print();
    }
}
