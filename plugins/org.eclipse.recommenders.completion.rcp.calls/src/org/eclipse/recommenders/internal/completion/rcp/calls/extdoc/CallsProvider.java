/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Patrick Gottschaemmer, Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.extdoc;

import static java.lang.String.format;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoBackgroundColor;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoForegroundColor;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.resolveMethod;
import static org.eclipse.swt.SWT.COLOR_INFO_FOREGROUND;

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
import org.eclipse.recommenders.completion.rcp.calls.l10n.Messages;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.recommenders.utils.annotations.Provisional;
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

@Provisional("Experimental code; needs to be cleaned up after provider layout is fixed.")
public final class CallsProvider extends ExtdocProvider {

    private final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore;
    private final JavaElementResolver jdtResolver;
    private final EventBus workspaceBus;
    private IType receiverType;
    private IObjectMethodCallsNet model;

    @Inject
    public CallsProvider(final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore,
            final JavaElementResolver jdtResolver, final EventBus workspaceBus) {
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
        final Optional<ASTNode> opt = event.getSelectedNode();
        if (!opt.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        final Optional<IType> varType = findVariableType(typeSignature, variable);
        if (!varType.isPresent()) {
            return Status.NOT_AVAILABLE;
        }

        receiverType = varType.get();
        if (!acquireModel()) {
            return Status.NOT_AVAILABLE;
        }
        try {
            final ASTNode node = opt.get();

            final Optional<MethodDeclaration> optAstMethod = findEnclosingMethod(node);
            final Optional<IMethod> optJdtMethod = resolveMethod(optAstMethod.orNull());
            if (!optJdtMethod.isPresent()) {
                return Status.NOT_AVAILABLE;
            }

            final AstBasedObjectUsageResolver r = new AstBasedObjectUsageResolver();
            final ObjectUsage usage = r.findObjectUsage(variable.getElementName(), optAstMethod.get());
            final IMethod first = JdtUtils.findFirstDeclaration(optJdtMethod.get());
            usage.contextFirst = jdtResolver.toRecMethod(first).or(VmMethodName.NULL);
            if (usage.kind == Kind.PARAMETER) {
                usage.definition = usage.contextFirst;
            }
            model.setQuery(usage);

            final Collection<Pair<IMethodName, Double>> methodCalls = model.getRecommendedMethodCalls(0.05d);
            final IMethodName ctx = model.getActiveContext();
            final IMethodName def = model.getActiveDefinition();
            final Kind kind = model.getActiveKind();
            final Set<IMethodName> calls = model.getActiveCalls();
            runSyncInUiThread(new CallRecommendationsRenderer(ctx, methodCalls, calls, variable.getElementName(), def,
                    kind, parent));
        } finally {
            releaseModel();
        }
        return Status.OK;
    }

    private Optional<IType> findVariableType(final String typeSignature, final IJavaElement parent) {
        final Optional<IType> varType = JdtUtils.findTypeFromSignature(typeSignature, parent);
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
        private final Collection<Pair<IMethodName, Double>> methodCalls;
        private final Set<IMethodName> calls;
        private final String varName;
        private final IMethodName def;
        private final Kind kind;
        private final Composite parent;

        private CallRecommendationsRenderer(final IMethodName ctx,
                final Collection<Pair<IMethodName, Double>> methodCalls, final Set<IMethodName> calls,
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
            final Composite container = ExtdocUtils.createComposite(parent, 4);
            final Label preamble2 = new Label(container, SWT.NONE);
            setInfoForegroundColor(preamble2);
            setInfoBackgroundColor(preamble2);
            preamble2.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 0).create());
            if (methodCalls.isEmpty()) {
                preamble2.setText(format(Messages.EXTDOC_RECOMMENDATIONS_ARE_NOT_MADE, receiverType.getElementName(),
                        varName));
            } else {
                preamble2.setText(format(Messages.EXTDOC_RECOMMENDATIONS_ARE_MADE, receiverType.getElementName(),
                        varName));
            }
            new Label(container, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 0)
                    .hint(SWT.DEFAULT, 1).create());
            for (final Pair<IMethodName, Double> rec : methodCalls) {
                final int percentage = (int) Math.rint(rec.getSecond() * 100);
                createLabel(container, ExtdocUtils.percentageToRecommendationPhrase(percentage), true, false,
                        COLOR_INFO_FOREGROUND, false);

                createLabel(container, Messages.EXTDOC_CALL + " ", false);//$NON-NLS-2$
                createMethodLink(container, rec.getFirst());
                createLabel(container, " - " + format(Messages.EXTDOC_PECOMMENDATION_PERCENTAGE, percentage), false);//$NON-NLS-1$
            }
            new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
            createLabel(container, "", false); //$NON-NLS-1$
            createLabel(container, "", false); //$NON-NLS-1$
            createLabel(container, "", false); //$NON-NLS-1$

            final Label preamble = new Label(container, SWT.NONE);
            preamble.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 5).create());
            setInfoForegroundColor(preamble);
            setInfoBackgroundColor(preamble);
            final String text;
            if (ctx == VmMethodName.NULL) {
                text = format(Messages.EXTDOC_PROPOSAL_COMPUTED_UNTRAINED, receiverType.getElementName());
            } else {
                text = format(Messages.EXTDOC_PROPOSAL_COMPUTED, receiverType.getElementName(),
                        Names.vm2srcSimpleTypeName(ctx.getDeclaringType()) + "." + Names.vm2srcSimpleMethod(ctx));
            }
            preamble.setText(text);

            new Label(container, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().span(4, 1).indent(0, 5)
                    .hint(SWT.DEFAULT, 1).create());

            if (def != null) {
                createLabel(container, Messages.EXTDOC_DEFINED_BY, true, false, SWT.COLOR_DARK_GRAY, false);
                createLabel(container, "", false, false, SWT.COLOR_DARK_GRAY, false); //$NON-NLS-1$
                if (def == VmMethodName.NULL) {
                    createLabel(container, Messages.EXTDOC_UNDEFINED, false, false, SWT.COLOR_DARK_GRAY, false);
                } else {
                    createMethodLink(container, def);
                }
                createLabel(container, "- " + kind.toString().toLowerCase(), true, false, SWT.COLOR_DARK_GRAY, false); //$NON-NLS-1$

            }

            for (final IMethodName observedCall : calls) {
                createLabel(container, Messages.EXTDOC_OBSERVED, true, false, SWT.COLOR_DARK_GRAY, false);

                createLabel(container, Messages.EXTDOC_CALL + " ", false, false, SWT.COLOR_DARK_GRAY, false);//$NON-NLS-2$
                createMethodLink(container, observedCall);
                createLabel(container, "", true, false, SWT.COLOR_DARK_GRAY, false); //$NON-NLS-1$
            }
        }

        Link createMethodLink(final Composite parent, final IMethodName method) {
            final String text = "<a>" + (method.isInit() ? "new " : "") + Names.vm2srcSimpleMethod(method) + "</a>"; // $NON-NLS$
            final String tooltip = Names.vm2srcQualifiedMethod(method);

            final Link link = new Link(parent, SWT.NONE);
            link.setText(text);
            setInfoBackgroundColor(link);
            link.setToolTipText(tooltip);
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    final Optional<IMethod> opt = jdtResolver.toJdtMethod(method);
                    if (opt.isPresent()) {
                        final JavaSelectionEvent event = new JavaSelectionEvent(opt.get(), METHOD_DECLARATION);
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
