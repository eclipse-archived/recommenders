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

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils.*;
import static org.eclipse.recommenders.utils.Bags.newHashMultiset;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.apidocs.ClassSelfCallsModelProvider;
import org.eclipse.recommenders.apidocs.ClassSelfcallDirectives;
import org.eclipse.recommenders.apidocs.MethodSelfCallsDirectivesModelProvider;
import org.eclipse.recommenders.apidocs.MethodSelfcallDirectives;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.UniqueMethodName;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.JavaElementResolver;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.Bags;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Multiset;
import com.google.common.eventbus.EventBus;

public final class SelfCallsProvider extends ApidocProvider {

    private final JavaElementResolver resolver;
    private final IProjectCoordinateProvider pcProvider;
    private final EventBus workspaceBus;

    private final ClassSelfCallsModelProvider cStore;
    private final MethodSelfCallsDirectivesModelProvider mStore;

    @Inject
    public SelfCallsProvider(JavaElementResolver resolver, IProjectCoordinateProvider pcProvider,
            EventBus workspaceBus, IModelRepository modelRepo, IModelIndex modelIndex,
            Map<String, IInputStreamTransformer> transformers) {
        this.resolver = resolver;
        this.pcProvider = pcProvider;
        this.workspaceBus = workspaceBus;
        mStore = new MethodSelfCallsDirectivesModelProvider(modelRepo, modelIndex, transformers);
        cStore = new ClassSelfCallsModelProvider(modelRepo, modelIndex, transformers);
    }

    @JavaSelectionSubscriber
    public void onTypeRootSelection(final ITypeRoot root, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onVariableSelection(ILocalVariable var, JavaElementSelectionEvent event, Composite parent)
            throws ExecutionException {
        IType type = ApidocsViewUtils.findType(var).orNull();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onVariableSelection(IField var, JavaElementSelectionEvent event, Composite parent)
            throws ExecutionException, JavaModelException {
        IType type = ApidocsViewUtils.findType(var).orNull();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaElementSelectionEvent event, final Composite parent)
            throws ExecutionException {
        UniqueTypeName name = pcProvider.toUniqueName(type).orNull();
        ClassSelfcallDirectives model = cStore.acquireModel(name).orNull();
        try {
            if (model != null) {
                runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, model, parent));
            }
        } finally {
            cStore.releaseModel(model);
        }
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaElementSelectionEvent event, final Composite parent) {

        for (IMethod current = method; current != null; current = JdtUtils.findOverriddenMethod(current).orNull()) {
            UniqueMethodName name = pcProvider.toUniqueName(current).orNull();
            MethodSelfcallDirectives selfcalls = mStore.acquireModel(name).orNull();
            try {
                if (selfcalls != null) {
                    runSyncInUiThread(new MethodSelfcallDirectivesRenderer(method, selfcalls, parent));
                }
            } finally {
                mStore.releaseModel(selfcalls);
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
            final String message = format(Messages.PROVIDER_INTRO_SUBCLASS_SELFCALL_STATISTICS,
                    directive.getNumberOfSubclasses(), type.getElementName());
            createLabel(container, message, true);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final Multiset<IMethodName> b = Bags.newHashMultiset(directive.getCalls());
            renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
                    Messages.TABLE_CELL_RELATION_CALL);
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
            final String message = format(Messages.PROVIDER_INTRO_IMPLEMENTOR_SELFCALL_STATISTIC,
                    directive.getNumberOfDefinitions(), method.getElementName());
            createLabel(container, message, true);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfDefinitions();
            final Multiset<IMethodName> b = newHashMultiset(directive.getCalls());
            renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver,
                    Messages.TABLE_CELL_RELATION_CALL);
        }
    }
}
