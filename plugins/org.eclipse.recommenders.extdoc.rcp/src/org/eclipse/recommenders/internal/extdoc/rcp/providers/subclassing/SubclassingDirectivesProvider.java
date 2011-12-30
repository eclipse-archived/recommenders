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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.subclassing;

import static java.lang.String.format;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils.createGridComposite;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils.createLabel;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.utils.TreeBag.newTreeBag;

import javax.inject.Inject;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.exceptions.NotFoundException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.google.common.eventbus.EventBus;

public final class SubclassingDirectivesProvider extends ExtdocProvider {

    private final WebServiceClient webclient;
    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;

    @Inject
    public SubclassingDirectivesProvider(@Extdoc final WebServiceClient webclient, final JavaElementResolver resolver,
            final EventBus workspaceBus) {
        this.webclient = webclient;
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;

    }

    @JavaSelectionSubscriber
    public void onTypeRootSelection(final ITypeRoot root, final JavaSelectionEvent event, final Composite parent) {
        final IType type = root.findPrimaryType();
        if (type != null) {
            onTypeSelection(type, event, parent);
        }
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent) {
        renderClassOverrideDirectives(type, parent);
        renderClassSelfcallDirectives(type, parent);
    }

    private void renderClassOverrideDirectives(final IType type, final Composite parent) {
        try {
            final ITypeName typeName = resolver.toRecType(type);
            final ClassOverrideDirectives overrides = webclient.doPostRequest("class-overrides", typeName,
                    ClassOverrideDirectives.class);
            runSyncInUiThread(new TypeOverrideDirectivesRenderer(type, overrides, parent));
        } catch (final NotFoundException e) {
            // TODO: responds to 404 sever reply AND service(!) not reachable!
        }
    }

    private void renderClassSelfcallDirectives(final IType type, final Composite parent) {
        try {
            final ITypeName typeName = resolver.toRecType(type);
            final ClassSelfcallDirectives selfcalls = webclient.doPostRequest("class-selfcalls", typeName,
                    ClassSelfcallDirectives.class);
            runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, selfcalls, parent));
        } catch (final NotFoundException e) {
            // TODO: responds to 404 sever reply AND service(!) not reachable!
        }
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaSelectionEvent event, final Composite parent) {

        for (IMethod current = method; current != null; current = JdtUtils.findOverriddenMethod(current).orNull()) {
            try {
                final IMethodName methodName = resolver.toRecMethod(current);
                final MethodSelfcallDirectives selfcalls = webclient.doPostRequest("method-selfcalls", methodName,
                        MethodSelfcallDirectives.class);
                runSyncInUiThread(new MethodSelfcallDirectivesRenderer(method, selfcalls, parent));
                break;
            } catch (final NotFoundException e) {
            }
        }
    }

    static String percentageToRecommendationPhrase(final int percentage) {
        if (percentage >= 95) {
            return "always";
        } else if (percentage >= 65) {
            return "usually";
        } else if (percentage >= 25) {
            return "sometimes";
        } else if (percentage >= 10) {
            return "occasionally";
        } else {
            return "rarely";
        }
    }

    Link createMethodLink(final Composite parent, final IMethodName method) {
        final String text = "<a>" + Names.vm2srcSimpleMethod(method) + "</a>";
        final String tooltip = Names.vm2srcQualifiedMethod(method);

        final Link link = new Link(parent, SWT.NONE);
        link.setText(text);
        link.setBackground(SwtUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
        link.setToolTipText(tooltip);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final IMethod jdtMethod = resolver.toJdtMethod(method);
                if (jdtMethod != null) {
                    final JavaSelectionEvent event = new JavaSelectionEvent(jdtMethod, METHOD_DECLARATION);
                    workspaceBus.post(event);
                } else {
                    link.setEnabled(false);
                }
            }
        });
        return link;
    }

    // ========================================================================
    // TODO: Review the renderer code is redundant and needs refactoring after all providers have been written to
    // identify more common parts.

    private class TypeOverrideDirectivesRenderer implements Runnable {

        private final IType type;
        private final ClassOverrideDirectives directive;
        private final Composite parent;
        private Composite container;

        public TypeOverrideDirectivesRenderer(final IType type, final ClassOverrideDirectives directive,
                final Composite parent) {
            this.type = type;
            this.directive = directive;
            this.parent = parent;
        }

        @Override
        public void run() {
            createContainer();
            addHeader();
            addDirectives();
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format("Based on %d direct subclasses of %s, we created the following statistics:",
                    directive.getNumberOfSubclasses(), type.getElementName());
            new Label(container, SWT.NONE).setText(message);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final TreeBag<IMethodName> b = newTreeBag(directive.getOverrides());

            final Composite group = createGridComposite(container, 4, 0, 0, 0, 0);
            for (final IMethodName method : b.elementsOrderedByFrequency()) {

                final int frequency = b.count(method);
                final int percentage = (int) Math.round(frequency * 100.0d / numberOfSubclasses);

                createLabel(group, "   " + percentageToRecommendationPhrase(percentage), true, false, SWT.COLOR_BLACK,
                        true);
                createLabel(group, "override", false);
                createMethodLink(group, method);
                createLabel(group, format(" -   (%d %% - %d times)", percentage, frequency), true);
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
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format("Based on %d direct subclasses of %s we created the following statistics:",
                    directive.getNumberOfSubclasses(), type.getElementName());
            new Label(container, SWT.NONE).setText(message);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfSubclasses();
            final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());

            final Composite group = createGridComposite(container, 4, 0, 0, 0, 0);
            for (final IMethodName method : b.elementsOrderedByFrequency()) {

                final int frequency = b.count(method);
                final int percentage = (int) Math.round(frequency * 100.0d / numberOfSubclasses);

                createLabel(group, "   " + percentageToRecommendationPhrase(percentage), true, false, SWT.COLOR_BLACK,
                        true);
                createLabel(group, "override", false);
                createMethodLink(group, method);
                createLabel(group, format(" -   (%d %% - %d times)", percentage, frequency), true);
            }
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
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addHeader() {
            final String message = format("Based on %d direct implementors of %s we created the following statistics:",
                    directive.getNumberOfDefinitions(), method.getElementName());
            new Label(container, SWT.NONE).setText(message);
        }

        private void addDirectives() {
            final int numberOfSubclasses = directive.getNumberOfDefinitions();
            final TreeBag<IMethodName> b = newTreeBag(directive.getCalls());

            final Composite group = createGridComposite(container, 4, 0, 0, 0, 0);
            for (final IMethodName method : b.elementsOrderedByFrequency()) {

                final int frequency = b.count(method);
                final int percentage = (int) Math.round(frequency * 100.0d / numberOfSubclasses);

                createLabel(group, "   " + percentageToRecommendationPhrase(percentage), true, false, SWT.COLOR_BLACK,
                        true);
                createLabel(group, "call", false);
                createMethodLink(group, method);
                createLabel(group, format(" -   (%d %% - %d times)", percentage, frequency), true);
            }
        }
    }
}
