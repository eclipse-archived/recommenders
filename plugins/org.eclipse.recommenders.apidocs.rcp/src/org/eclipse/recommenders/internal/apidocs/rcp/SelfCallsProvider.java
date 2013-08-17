/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static com.google.common.base.Optional.*;
import static java.lang.String.format;
import static org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils.*;
import static org.eclipse.recommenders.utils.Bags.newHashMultiset;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.apidocs.ClassSelfcallDirectives;
import org.eclipse.recommenders.apidocs.MethodSelfcallDirectives;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.models.UniqueMethodName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.Bags;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Optional;
import com.google.common.collect.Multiset;
import com.google.common.eventbus.EventBus;

public final class SelfCallsProvider extends ApidocProvider {

    @Inject
    JavaElementResolver resolver;
    @Inject
    IProjectCoordinateProvider pcProvider;
    @Inject
    EventBus workspaceBus;
    @Inject
    ClassSelfCallsModelProvider cStore;
    @Inject
    MethodSelfCallsDirectivesModelProvider mStore;

    @JavaSelectionSubscriber
    public void onTypeRootSelection(final ITypeRoot root, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        UniqueTypeName name = pcProvider.toUniqueName(type).orNull();
        Optional<ClassSelfcallDirectives> model = cStore.acquireModel(name);
        if (model.isPresent()) {
            runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, model.get(), parent));
        }
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaElementSelectionEvent event, final Composite parent) {

        for (IMethod current = method; current != null; current = JdtUtils.findOverriddenMethod(current).orNull()) {
            UniqueMethodName name = pcProvider.toUniqueName(current).orNull();
            final Optional<MethodSelfcallDirectives> selfcalls = mStore.acquireModel(name);
            if (selfcalls.isPresent()) {
                runSyncInUiThread(new MethodSelfcallDirectivesRenderer(method, selfcalls.get(), parent));
            }
        }
    }

    private class TypeSelfcallDirectivesRenderer implements Runnable {

        private final IType type;
        private final ClassSelfcallDirectives directive;
        private final Composite parent;
        private Composite container;

        public TypeSelfcallDirectivesRenderer(final IType type, final ClassSelfcallDirectives selfcalls,
                final Composite parent) {
            this.type = type;
            directive = selfcalls;
            this.parent = parent;
        }

        @Override
        public void run() {
            createContainer();
            addHeader();
            addDirectives();
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NONE);
            setInfoBackgroundColor(container);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format(Messages.EXTDOC_SELFCALLS_INTRO_SUBCLASSES,
                    directive.getNumberOfSubclasses(), type.getElementName());
            createLabel(container, message, true);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final Multiset<IMethodName> b = Bags.newHashMultiset(directive.getCalls());
            renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
                    Messages.EXTDOC_SELFCALLS_CALLS);
        }
    }

    private class MethodSelfcallDirectivesRenderer implements Runnable {

        private final IMethod method;
        private final MethodSelfcallDirectives directive;
        private final Composite parent;

        private Composite container;

        public MethodSelfcallDirectivesRenderer(final IMethod method, final MethodSelfcallDirectives selfcalls,
                final Composite parent) {
            this.method = method;
            directive = selfcalls;
            this.parent = parent;
        }

        @Override
        public void run() {
            createContainer();
            addHeader();
            addDirectives();
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NONE);
            setInfoBackgroundColor(container);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format(Messages.EXTDOC_SELFCALLS_INTRO_IMPLEMENTORS,
                    directive.getNumberOfDefinitions(), method.getElementName());
            createLabel(container, message, true);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfDefinitions();
            final Multiset<IMethodName> b = newHashMultiset(directive.getCalls());
            renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
                    Messages.EXTDOC_SELFCALLS_CALLS);
        }
    }

    public static class ClassSelfCallsModelProvider extends
            PoolingModelProvider<UniqueTypeName, ClassSelfcallDirectives> {

        @Inject
        public ClassSelfCallsModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index) {
            super(repository, index, Constants.CLASS_SELFC_MODEL);
        }

        @Override
        protected Optional<ClassSelfcallDirectives> loadModel(ZipFile zip, UniqueTypeName key) throws Exception {
            String path = Zips.path(key.getName(), ".json");
            ZipEntry entry = zip.getEntry(path);
            if (entry == null) {
                return absent();
            }
            InputStream is = zip.getInputStream(entry);
            ClassSelfcallDirectives res = GsonUtil.deserialize(is, ClassSelfcallDirectives.class);
            IOUtils.closeQuietly(is);
            return of(res);
        }
    }

    public static class MethodSelfCallsDirectivesModelProvider extends
            PoolingModelProvider<UniqueMethodName, MethodSelfcallDirectives> {

        @Inject
        public MethodSelfCallsDirectivesModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index) {
            super(repository, index, Constants.CLASS_SELFM_MODEL);
        }

        @Override
        protected Optional<MethodSelfcallDirectives> loadModel(ZipFile zip, UniqueMethodName key) throws Exception {
            String path = Zips.path(key.getName(), ".json");
            ZipEntry entry = zip.getEntry(path);
            if (entry == null) {
                return absent();
            }
            InputStream is = zip.getInputStream(entry);
            MethodSelfcallDirectives res = GsonUtil.deserialize(is, MethodSelfcallDirectives.class);
            IOUtils.closeQuietly(is);
            return of(res);
        }
    }
}
