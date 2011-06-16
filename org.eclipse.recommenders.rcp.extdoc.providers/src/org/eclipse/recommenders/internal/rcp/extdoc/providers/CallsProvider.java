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
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
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
    private TextAndFeaturesLine line;
    private Composite calls;

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
        } else if (element instanceof ILocalVariable || element instanceof SourceField) {
            try {
                return displayProposalsForVariable(element);
            } catch (final JavaModelException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private boolean displayProposalsForType(final IType type) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType(type);
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = getModel(typeName);
            final boolean success = displayProposals(type, model.getRecommendedMethodCalls(0.01, 5));
            modelStore.releaseModel(model);
            return success;
        }
        return false;
    }

    private boolean displayProposalsForMethod(final IMethod method) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecMethod(method).getDeclaringType();
        if (modelStore.hasModel(typeName)) {
            final IObjectMethodCallsNet model = getModel(typeName);
            final boolean success = displayProposals(method, model.getRecommendedMethodCalls(0.01, 5));
            modelStore.releaseModel(model);
            return success;
        }
        return false;
    }

    private boolean displayProposalsForVariable(final IJavaElement element) throws JavaModelException {
        final ITypeName simpleTypeName = findVariableType(element);
        if (simpleTypeName != null) {
            for (final ITypeName typeName : modelStore.findTypesBySimpleName(simpleTypeName)) {
                final IObjectMethodCallsNet model = getModel(typeName);
                final boolean success = displayProposals(element, model.getRecommendedMethodCalls(0.01, 5));
                modelStore.releaseModel(model);
                return success;
            }
        }
        return false;
    }

    private ITypeName findVariableType(final IJavaElement element) throws JavaModelException {
        final String signature;
        if (element instanceof SourceField) {
            signature = ((SourceField) element).getTypeSignature();
        } else {
            signature = ((ILocalVariable) element).getTypeSignature();
        }
        if (signature.length() < 4) {
            return null;
        }
        return VmTypeName.get(signature.substring(0, signature.length() - 1));
    }

    private IObjectMethodCallsNet getModel(final ITypeName typeName) {
        final IObjectMethodCallsNet model = modelStore.acquireModel(typeName);
        model.clearEvidence();
        model.setMethodContext(null);
        model.updateBeliefs();
        return model;
    }

    private boolean displayProposals(final IJavaElement element, final SortedSet<Tuple<IMethodName, Double>> proposals) {
        if (proposals.isEmpty()) {
            return false;
        }
        if (calls != null) {
            calls.dispose();
            line.dispose();
        }

        final String text = "People who use " + element.getElementName() + " usually also call the following methods:";
        line = new TextAndFeaturesLine(composite, text, element, element.getElementName(), this, server,
                new TemplateEditDialog(getShell()));
        line.createStyleRange(15, element.getElementName().length(), SWT.NORMAL, false, true);

        calls = SwtFactory.createGridComposite(composite, 3, 12, 3, 12, 0);
        for (final Tuple<IMethodName, Double> proposal : proposals) {
            SwtFactory.createSquare(calls);
            SwtFactory.createLabel(calls, Names.vm2srcSimpleMethod(proposal.getFirst()), false, false, true);
            SwtFactory.createLabel(calls, Math.round(proposal.getSecond() * 100) + "%", false, true, false);
        }

        composite.layout(true);
        return true;
    }
}
