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
package org.eclipse.recommenders.internal.completion.rcp.calls.extdoc;

import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;

import java.util.Collection;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.bak.IProjectModelFacade;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.bak.ProjectServices;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.annotations.Experimental;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

/**
 * Experimental code.
 */
@Experimental
public final class CallsProvider extends ExtdocProvider {

    private final ProjectServices projectServices;
    private final JavaElementResolver jdtResolver;
    private final EventBus workspaceBus;
    private IJavaProject javaProject;
    private IType receiverType;
    private IObjectMethodCallsNet model;

    @Inject
    public CallsProvider(final ProjectServices projectServices, final JavaElementResolver jdtResolver,
            final EventBus workspaceBus) {
        this.projectServices = projectServices;
        this.jdtResolver = jdtResolver;
        this.workspaceBus = workspaceBus;
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
        Optional<MethodDeclaration> optMethodDeclaration = findEnclosingMethod(node);
        Optional<IMethod> optMethod = JdtUtils.resolveMethod(optMethodDeclaration.orNull());
        if (!optMethod.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
        final ObjectUsage usage = r.findObjectUsage(type.getElementName(), optMethodDeclaration.get());
        IMethod first = JdtUtils.findFirstDeclaration(optMethod.get());
        usage.contextFirst = jdtResolver.toRecMethod(first);
        if (usage.kind == Kind.PARAMETER) {
            usage.definition = usage.contextFirst;
        }
        model.setQuery(usage);
        final Collection<Tuple<IMethodName, Double>> methodCalls = model.getRecommendedMethodCalls(0.05d);
        releaseModel();
        runSyncInUiThread(new Runnable() {

            @Override
            public void run() {
                Composite container = ExtdocUtils.createComposite(parent, 4);
                for (Tuple<IMethodName, Double> rec : methodCalls) {
                    int percentage = (int) Math.rint(rec.getSecond() * 100);
                    createLabel(container, ExtdocUtils.percentageToRecommendationPhrase(percentage), true, false,
                            SWT.COLOR_BLACK, false);

                    createLabel(container, "call", false);
                    createMethodLink(container, rec.getFirst());
                    createLabel(container, " - " + percentage + "%", false);
                }
                new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
                createLabel(container, "", false);
                createLabel(container, "", false);
                createLabel(container, "", false);
                if (usage.definition != null) {
                    createLabel(container, "defined by", true, false, SWT.COLOR_DARK_GRAY, false);
                    createLabel(container, " call", false, false, SWT.COLOR_DARK_GRAY, false);
                    createMethodLink(container, usage.definition);
                    createLabel(container, "- " + usage.kind.toString().toLowerCase(), true, false,
                            SWT.COLOR_DARK_GRAY, false);

                }

                for (IMethodName observedCall : usage.calls) {
                    createLabel(container, "observed", true, false, SWT.COLOR_DARK_GRAY, false);

                    createLabel(container, " call", false, false, SWT.COLOR_DARK_GRAY, false);
                    createMethodLink(container, observedCall);
                    createLabel(container, "", true, false, SWT.COLOR_DARK_GRAY, false);
                }
            }

            Link createMethodLink(final Composite parent, final IMethodName method) {
                final String text = "<a>" + (method.isInit() ? "new " : "") + Names.vm2srcSimpleMethod(method) + "</a>";
                final String tooltip = Names.vm2srcQualifiedMethod(method);

                final Link link = new Link(parent, SWT.NONE);
                link.setText(text);
                link.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
                link.setToolTipText(tooltip);
                link.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        final IMethod jdtMethod = jdtResolver.toJdtMethod(method);
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
        });
        return Status.OK;
    }

    private Optional<MethodDeclaration> findEnclosingMethod(final ASTNode node) {
        MethodDeclaration declaringNode = null;
        for (ASTNode p = node; p != null; p = p.getParent()) {
            if (p instanceof MethodDeclaration) {
                declaringNode = (MethodDeclaration) p;
                break;
            }
        }
        return Optional.fromNullable(declaringNode);
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
}
