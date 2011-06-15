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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import java.util.SortedSet;

import com.google.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class CallsProvider extends AbstractProviderComposite {

    private final CallsModelStore modelStore;
    private final CallsServer server;

    private Composite composite;
    private Composite container;

    @Inject
    public CallsProvider(final CallsModelStore modelStore, final CallsServer server) {
        this.modelStore = modelStore;
        this.server = server;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 11, 0, 0);
        return composite;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayProposalsForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayProposalsForMethod((IMethod) element);
        }
        return displayProposalsForVariable(element);
    }

    private boolean displayProposalsForType(final IType type) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
            displayProposals(type, model.getRecommendedMethodCalls(0.05, 5));
            modelStore.releaseModel(model);
            return true;
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType((IType) method.getParent());
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
            displayProposals(method, model.getRecommendedMethodCalls(0.01));
            modelStore.releaseModel(model);
            return true;
        }
        return false;
    }

    private boolean displayProposalsForVariable(final IJavaElement element) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType((IType) element.getParent());
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
            displayProposals(element, model.getRecommendedMethodCalls(0.01));
            modelStore.releaseModel(model);
            return true;
        }
        return false;
    }

    private boolean displayProposals(final IJavaElement element, final SortedSet<Tuple<IMethodName, Double>> proposals) {
        if (container != null) {
            container.dispose();
        }
        container = new Composite(composite, SWT.NONE);

        final String text = "By analyzing XXX occasions of " + element.getElementName()
                + ", the following patterns have been identified:";
        final TextAndFeaturesLine line = new TextAndFeaturesLine(container, text, element, element.getElementName(),
                this, server, new TemplateEditDialog(getShell()));
        line.createStyleRange(30, element.getElementName().length(), SWT.NORMAL, false, true);

        final Composite calls = SwtFactory.createGridComposite(container, 3, 12, 3, 12, 0);
        for (final Tuple<IMethodName, Double> proposal : proposals) {
            SwtFactory.createSquare(calls);
            SwtFactory.createLabel(calls, proposal.getFirst().getIdentifier(), false, false, false);
            SwtFactory.createLabel(calls, proposal.getSecond() * 100 + "%", false, true, false);
        }

        composite.layout(true);
        return true;
    }
}
