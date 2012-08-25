/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import static java.lang.String.format;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoBackgroundColor;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoForegroundColor;
import static org.eclipse.recommenders.utils.TreeBag.newTreeBag;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.extdoc.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.ClassSelfcallsModelStore;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.MethodSelfcallsModelStore;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

public final class SelfCallsProvider extends ExtdocProvider {

    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;
    private final ClassSelfcallsModelStore cStore;
    private final MethodSelfcallsModelStore mStore;

    @Inject
    public SelfCallsProvider(ClassSelfcallsModelStore cStore, MethodSelfcallsModelStore mStore,
            final JavaElementResolver resolver, final EventBus workspaceBus) {
        this.cStore = cStore;
        this.mStore = mStore;
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;

    }

    @JavaSelectionSubscriber
    public void onTypeRootSelection(final ITypeRoot root, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {
        final IType type = root.findPrimaryType();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {
        Optional<ClassSelfcallDirectives> model = cStore.aquireModel(type);
        if (model.isPresent()) {
            runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, model.get(), parent));
        }
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaSelectionEvent event, final Composite parent) {

        for (IMethod current = method; current != null; current = JdtUtils.findOverriddenMethod(current).orNull()) {
            final Optional<MethodSelfcallDirectives> selfcalls = mStore.aquireModel(current);
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
            this.directive = selfcalls;
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
            final String message = format("Based on %d direct subclasses of %s we created the following statistics:",
                    directive.getNumberOfSubclasses(), type.getElementName());
            Label label = new Label(container, SWT.NONE);
            label.setText(message);
            setInfoForegroundColor(label);
            setInfoBackgroundColor(label);
            label.setFont(JFaceResources.getDialogFont());
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());
            ExtdocUtils.renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver, "calls ");
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
            this.directive = selfcalls;
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
            final String message = format(
                    "Based on %d direct implementors of %s we created the following statistics. Implementors...",
                    directive.getNumberOfDefinitions(), method.getElementName());
            Label label = new Label(container, SWT.NONE);
            label.setText(message);
            setInfoForegroundColor(label);
            setInfoBackgroundColor(label);
            label.setFont(JFaceResources.getDialogFont());
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfDefinitions();
            final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());
            ExtdocUtils.renderMethodDirectivesBlock(container, b, numberOfSubclasses, workspaceBus, resolver, "calls ");
        }
    }
}
