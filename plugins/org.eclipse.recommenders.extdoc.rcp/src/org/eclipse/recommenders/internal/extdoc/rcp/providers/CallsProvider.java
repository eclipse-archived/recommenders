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

import static org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils.createLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils.createSourceCodeArea;

import java.util.Collection;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.extdoc.CodeExamples;
import org.eclipse.recommenders.extdoc.CodeSnippet;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.bak.IProjectModelFacade;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.bak.ProjectServices;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.SwtUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Optional;

public final class CallsProvider extends ExtdocProvider {

    private final ProjectServices projectServices;
    private final JavaElementResolver jdtResolver;
    private IJavaProject javaProject;
    private IType receiverType;
    private IObjectMethodCallsNet model;

    @Inject
    public CallsProvider(final ProjectServices projectServices, final JavaElementResolver jdtResolver) {
        this.projectServices = projectServices;
        this.jdtResolver = jdtResolver;
    }

    @JavaSelectionSubscriber
    public Status onVariableSelection(final ILocalVariable type, final JavaSelectionEvent event, final Composite parent) {

        Optional<ASTNode> opt = event.getSelectedNode();
        if (!opt.isPresent()) {
            return Status.NOT_AVAILABLE;
        }
        javaProject = type.getJavaProject();
        String typeSignature = type.getTypeSignature();
        Optional<IType> varType = JdtUtils.findTypeFromSignature(typeSignature, type);
        if (!varType.isPresent()) {
            return Status.NOT_AVAILABLE;
        }
        receiverType = varType.get();
        if (!acquireModel()) {
            return Status.NOT_AVAILABLE;
        }

        ASTNode node = opt.get();
        MethodDeclaration method = null;
        for (ASTNode p = node; p != null; p = p.getParent()) {
            if (p instanceof MethodDeclaration) {
                method = (MethodDeclaration) p;
                break;
            }
        }
        if (method == null) {
            return Status.NOT_AVAILABLE;
        }

        final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
        final ObjectUsage usage = r.findObjectUsage(type.getElementName(), method);

        model.setQuery(usage);
        final Collection<Tuple<IMethodName, Double>> methodCalls = model.getRecommendedMethodCalls(0.05d);
        releaseModel();
        runSyncInUiThread(new Runnable() {

            @Override
            public void run() {
                Composite container = SwtUtils.createComposite(parent);
                createLabel(container, "work in progress", true, false, SWT.COLOR_DARK_RED, true);
                for (Tuple<IMethodName, Double> rec : methodCalls) {
                    int percentage = (int) Math.rint(rec.getSecond() * 100);
                    String text = rec.getFirst().getName() + " - " + percentage + "%";
                    createLabel(container, text, false, true, SWT.COLOR_BLACK, false);
                }

            }
        });
        return Status.OK;
    }

    private boolean acquireModel() {

        final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);

        final ITypeName recReceiverType = jdtResolver.toRecType(receiverType);
        if (modelFacade.hasModel(recReceiverType)) {
            model = modelFacade.acquireModel(recReceiverType);
        }
        return model != null;
    }

    private void releaseModel() {
        if (model != null) {
            final IProjectModelFacade modelFacade = projectServices.getModelFacade(javaProject);
            modelFacade.releaseModel(model);
            model = null;
        }
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
            container = new Composite(parent, SWT.NO_BACKGROUND);
            container.setLayout(new GridLayout());
        }

        private void addDirectives() {

            for (final CodeSnippet snippet : examples.getExamples()) {
                createSourceCodeArea(container, snippet.getCode());
            }
        }
    }

}
