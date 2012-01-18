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

import static java.lang.String.format;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.resolveMethod;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.annotations.Experimental;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
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
 * <p>
 * XXX MB: this is dark force code... need to clean it up after provider layout fixed.
 */
@Experimental
public final class CallsProvider extends ExtdocProvider {

    private final CallModelStore modelStore;
    private final JavaElementResolver jdtResolver;
    private final EventBus workspaceBus;
    private IType receiverType;
    private IObjectMethodCallsNet model;

    @Inject
    public CallsProvider(final CallModelStore modelStore, final JavaElementResolver jdtResolver,
            final EventBus workspaceBus) {
        this.modelStore = modelStore;
        this.jdtResolver = jdtResolver;
        this.workspaceBus = workspaceBus;
    }

    @JavaSelectionSubscriber
    public Status onVariableSelection(final ILocalVariable var, final JavaSelectionEvent event, final Composite parent) {
        return handle(var, var.getElementName(), var.getTypeSignature(), event, parent);
    }

    @JavaSelectionSubscriber(METHOD_BODY)
    public Status onFieldSelection(final IField var, final JavaSelectionEvent event, final Composite parent)
            throws JavaModelException {
        return handle(var, var.getElementName(), var.getTypeSignature(), event, parent);
    }

    private Status handle(final IJavaElement variable, final String elementName, final String typeSignature,
            final JavaSelectionEvent event, final Composite parent) {
        Optional<ASTNode> opt = event.getSelectedNode();
        if (!opt.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        Optional<IType> varType = findVariableType(typeSignature, variable);
        if (!varType.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        receiverType = varType.get();
        if (!acquireModel()) {
            return Status.NOT_AVAILABLE;
        }
        ASTNode node = opt.get();

        Optional<MethodDeclaration> optAstMethod = findEnclosingMethod(node);
        Optional<IMethod> optJdtMethod = resolveMethod(optAstMethod.orNull());
        if (!optJdtMethod.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
        ObjectUsage usage = r.findObjectUsage(variable.getElementName(), optAstMethod.get());
        IMethod first = JdtUtils.findFirstDeclaration(optJdtMethod.get());
        usage.contextFirst = jdtResolver.toRecMethod(first);
        if (usage.kind == Kind.PARAMETER) {
            usage.definition = usage.contextFirst;
        }
        model.setQuery(usage);

        final Collection<Tuple<IMethodName, Double>> methodCalls = model.getRecommendedMethodCalls(0.05d);
        final IMethodName ctx = model.getActiveContext();
        final IMethodName def = model.getActiveDefinition();
        final Kind kind = model.getActiveKind();
        final Set<IMethodName> calls = model.getActiveCalls();
        releaseModel();
        runSyncInUiThread(new CallRecommendationsRenderer(ctx, methodCalls, calls, variable.getElementName(), def,
                kind, parent));
        return Status.OK;
    }

    private Optional<IType> findVariableType(final String typeSignature, final IJavaElement parent) {
        Optional<IType> varType = JdtUtils.findTypeFromSignature(typeSignature, parent);
        return varType;
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
        model = modelStore.aquireModel(receiverType).orNull();
        return model != null;
    }

    private void releaseModel() {
        if (model != null) {
            modelStore.releaseModel(model);
            model = null;
        }
    }

    private final class CallRecommendationsRenderer implements Runnable {
        private final IMethodName ctx;
        private final Collection<Tuple<IMethodName, Double>> methodCalls;
        private final Set<IMethodName> calls;
        private final String varName;
        private final IMethodName def;
        private final Kind kind;
        private final Composite parent;

        private CallRecommendationsRenderer(final IMethodName ctx,
                final Collection<Tuple<IMethodName, Double>> methodCalls, final Set<IMethodName> calls,
                final String varName, final IMethodName def, final Kind kind, final Composite parent) {
            this.ctx = ctx;
            this.methodCalls = methodCalls;
            this.calls = calls;
            this.varName = varName;
            this.def = def;
            this.kind = kind;
            this.parent = parent;
        }

        @Override
        public void run() {
            Composite container = ExtdocUtils.createComposite(parent, 4);
            Label preamble2 = new Label(container, SWT.NONE);
            preamble2.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 0).create());
            if (methodCalls.isEmpty()) {
                preamble2.setText(format("For %s %s no recommendations are made.", receiverType.getElementName(),
                        varName));
            } else {
                preamble2.setText(format("For %s %s the following recommendations are made:",
                        receiverType.getElementName(), varName));
            }
            new Label(container, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 0)
                    .hint(SWT.DEFAULT, 1).create());
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

            Label preamble = new Label(container, SWT.NONE);
            preamble.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 5).create());
            String text = format("Proposals were computed based on variable type '%s' in '%s'.",
                    receiverType.getElementName(),
                    ctx == VmMethodName.NULL ? "untrained context" : Names.vm2srcSimpleTypeName(ctx.getDeclaringType())
                            + "." + Names.vm2srcSimpleMethod(ctx));
            preamble.setText(text);

            new Label(container, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 5)
                    .hint(SWT.DEFAULT, 1).create());

            if (def != null) {
                createLabel(container, "defined by", true, false, SWT.COLOR_DARK_GRAY, false);
                createLabel(container, "", false, false, SWT.COLOR_DARK_GRAY, false);
                if (def == VmMethodName.NULL) {
                    createLabel(container, "untrained definition", false, false, SWT.COLOR_DARK_GRAY, false);
                } else {
                    createMethodLink(container, def);
                }
                createLabel(container, "- " + kind.toString().toLowerCase(), true, false, SWT.COLOR_DARK_GRAY, false);

            }

            for (IMethodName observedCall : calls) {
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
    }
}
