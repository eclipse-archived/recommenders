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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.VisitorImpl;
import org.eclipse.recommenders.utils.AnonymousId;
import org.eclipse.recommenders.utils.gson.EmfFieldExclusionStrategy;
import org.eclipse.recommenders.utils.gson.UuidTypeAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ErrorReports {

    public static final class ClearMessagesVisitor extends VisitorImpl {
        @Override
        public void visit(Status status) {
            status.setMessage(HIDDEN);
        }

        @Override
        public void visit(Throwable throwable) {
            throwable.setMessage(HIDDEN);
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
                throwable.setClassName(HIDDEN);
            }
        }

        @Override
        public void visit(StackTraceElement element) {
            if (!isWhitelisted(element.getClassName(), whitelist)) {
                element.setClassName(HIDDEN);
                element.setMethodName(HIDDEN);
                element.setFileName(HIDDEN);
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

    static boolean isWhitelisted(String className, List<String> whitelist) {
        for (String whiteListedPrefix : whitelist) {
            if (className.startsWith(whiteListedPrefix)) {
                return true;
            }
        }
        return false;
    }

    private static final String HIDDEN = "HIDDEN";
    private static ModelFactory factory = ModelFactory.eINSTANCE;

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
        mReport.setOsgiOs(System.getProperty(Constants.FRAMEWORK_OS_NAME, "-"));
        mReport.setOsgiOsVersion(System.getProperty(Constants.FRAMEWORK_OS_VERSION, "-"));
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

    public static Status newStatus(IStatus status, Settings settings) {
        Status mStatus = factory.createStatus();
        mStatus.setMessage(status.getMessage());
        mStatus.setSeverity(status.getSeverity());
        mStatus.setCode(status.getCode());
        mStatus.setPluginId(status.getPlugin());

        Bundle bundle = Platform.getBundle(status.getPlugin());
        if (bundle != null) {
            mStatus.setPluginVersion(bundle.getVersion().toString());
        }

        List<Status> mChildren = mStatus.getChildren();
        for (IStatus child : status.getChildren()) {
            mChildren.add(newStatus(child, settings));
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
}
