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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.examples;

import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createSourceCodeArea;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoBackgroundColor;

import javax.inject.Inject;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.extdoc.CodeExamples;
import org.eclipse.recommenders.extdoc.CodeSnippet;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.ExtdocResourceProxy;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public final class ExamplesProvider extends ExtdocProvider {

    private final ExtdocResourceProxy proxy;
    private final JavaElementResolver resolver;

    @Inject
    public ExamplesProvider(final ExtdocResourceProxy proxy, final JavaElementResolver resolver) {
        this.proxy = proxy;
        this.resolver = resolver;

    }

    @JavaSelectionSubscriber
    public Status onTypeRootSelection(final ITypeRoot root, final JavaSelectionEvent event, final Composite parent) {
        final IType type = root.findPrimaryType();
        if (type != null) {
            return onTypeSelection(type, event, parent);
        }
        return Status.NOT_AVAILABLE;
    }

    @JavaSelectionSubscriber
    public Status onTypeSelection(final IType type, final JavaSelectionEvent event, final Composite parent) {
        final ITypeName typeName = resolver.toRecType(type);
        final CodeExamples examples = proxy.findCodeExamples(typeName);
        if (examples == null) {
            return Status.NOT_AVAILABLE;
        }
        runSyncInUiThread(new TypeSelfcallDirectivesRenderer(type, examples, parent));
        return Status.OK;
    }

    private class TypeSelfcallDirectivesRenderer implements Runnable {

        private final CodeExamples examples;
        private final Composite parent;
        private Composite container;

        public TypeSelfcallDirectivesRenderer(final IType type, final CodeExamples examples, final Composite parent) {
            this.examples = examples;
            this.parent = parent;
        }

        @Override
        public void run() {
            createContainer();
            addDirectives();
        }

        private void createContainer() {
            container = new Composite(parent, SWT.NONE);
            setInfoBackgroundColor(container);
            container.setLayout(new GridLayout());
        }

        private void addDirectives() {

            for (final CodeSnippet snippet : examples.getExamples()) {
                createSourceCodeArea(container, snippet.getCode());
            }
        }
    }

}
