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

import static com.google.common.base.Optional.fromNullable;
import static org.apache.commons.lang3.StringUtils.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
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
import com.google.common.base.Optional;
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

        public PrettyPrintVisitor() {
            bundlesStringBuilder = new StringBuilder();
            appendHeadline("BUNDLES", bundlesStringBuilder);
        }

        private void appendAttributes(EObject object, StringBuilder builder) {
            for (EAttribute attribute : object.eClass().getEAllAttributes()) {
                String line = String.format("%-" + RIGHT_PADDING + "s", attribute.getName() + ":")
                        + object.eGet(attribute) + "\n";
                builder.append(line);
            }
            builder.append("\n");
        }

        private void appendHeadline(String headline, StringBuilder builder) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
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
            statusStringBuilder.append("Exception:");
            Throwable exception = status.getException();
            if (exception != null) {
                append(exception, statusStringBuilder);
            } else {
                statusStringBuilder.append("null");
            }
            super.visit(status);
        }

        private void append(Throwable throwable, StringBuilder builder) {
            builder.append(String.format("%s: %s\n", throwable.getClassName(), throwable.getMessage()));
            for (StackTraceElement element : throwable.getStackTrace()) {
                builder.append(String.format("\t at %s.%s(%s:%s)\n", element.getClassName(), element.getMethodName(),
                        element.getFileName(), element.getLineNumber()));
            }
            Throwable cause = throwable.getCause();
            if (cause != null) {
                statusStringBuilder.append("Caused by: ");
                append(cause, builder);
            }
        }

        @Override
        public void visit(org.eclipse.recommenders.internal.stacktraces.rcp.model.Bundle bundle) {
            appendAttributes(bundle, bundlesStringBuilder);
            super.visit(bundle);
        }

        public String print() {
            return new StringBuilder().append(statusStringBuilder).append("\n").append(reportStringBuilder)
                    .append(bundlesStringBuilder).toString();
        }

    }

    private static class MultiStatusFilter {

        private static void filter(Status status) {
            HashSet<Throwable> throwables = new HashSet<Throwable>();
            filter(status, throwables);
        }

        private static void filter(Status status, Set<Throwable> throwables) {
            EList<Status> children = status.getChildren();
            int removedCount = 0;
            for (int i = children.size() - 1; i >= 0; i--) {
                Status childStatus = children.get(i);
                if (filterChild(childStatus, throwables)) {
                    children.remove(i);
                    removedCount++;
                } else {
                    filter(childStatus, throwables);
                }
            }
            if (removedCount > 0) {
                status.setMessage(String.format("%s [%d child-status duplicates removed by Error Reporting]",
                        status.getMessage(), removedCount));
            }

        }

        private static boolean filterChild(Status status, Set<Throwable> throwables) {
            Throwable throwable = status.getException();
            if (throwable.getStackTrace().isEmpty()) {
                return true;
            }
            for (Throwable t : throwables) {
                if (stackTraceMatches(throwable, t)) {
                    return true;
                }
            }
            throwables.add(throwable);
            return false;
        }

        private static boolean stackTraceMatches(Throwable throwable, Throwable t) {
            EList<StackTraceElement> stackTrace = throwable.getStackTrace();
            EList<StackTraceElement> stackTrace2 = t.getStackTrace();
            if (stackTrace.size() != stackTrace2.size()) {
                return false;
            }
            for (int i = 0; i < stackTrace.size(); i++) {
                StackTraceElement ste = stackTrace.get(i);
                StackTraceElement ste2 = stackTrace2.get(i);
                if (!(ste.getClassName().equals(ste2.getClassName()) && ste.getMethodName()
                        .equals(ste2.getMethodName()))) {
                    return false;
                }
            }
            return true;
        }

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
        mReport.setEclipseBuildId(getEclipseBuildId().or("-"));
        mReport.setEclipseProduct(System.getProperty("eclipse.product", "-"));
        mReport.setOsgiArch(System.getProperty("osgi.arch", "-"));
        mReport.setOsgiWs(System.getProperty("osgi.ws", "-"));
        mReport.setOsgiOs(System.getProperty(org.osgi.framework.Constants.FRAMEWORK_OS_NAME, "-"));
        mReport.setOsgiOsVersion(System.getProperty(org.osgi.framework.Constants.FRAMEWORK_OS_VERSION, "-"));
        mReport.setStatus(newStatus(event, settings));

        guessInvolvedPlugins(mReport);
        return mReport;
    }

    public static Optional<String> getEclipseBuildId() {
        String res = System.getProperty("eclipse.buildId");
        return fromNullable(res);
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
        // Multistatus handling
        for (IStatus child : status.getChildren()) {
            mChildren.add(newStatus(child, settings));
        }
        // some stacktraces from ui.monitoring should be filtered
        boolean needFiltering = "org.eclipse.ui.monitoring".equals(status.getPlugin())
                && (status.getCode() == 0 || status.getCode() == 1);
        if (needFiltering) {
            MultiStatusFilter.filter(mStatus);
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

    private static String removeSourceFileContents(String message) {
        if (message.contains(Constants.SOURCE_BEGIN_MESSAGE)) {
            return Constants.SOURCE_FILE_REMOVED;
        } else {
            return message;
        }
    }

    public static Throwable newThrowable(java.lang.Throwable throwable) {
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

    public static String getFingerprint(ErrorReport report) {
        return report.getStatus().getFingerprint();
    }
}
