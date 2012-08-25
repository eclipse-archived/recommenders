/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.ui;

import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoBackgroundColor;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoForegroundColor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.rcp.PartListener2Adapter;
import org.eclipse.recommenders.utils.rcp.RCPUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ExtdocView extends ViewPart {

    public static final String ID = "org.eclipse.recommenders.extdoc.rcp.ExtdocView";

    private final EventBus workspaceBus;
    private final SubscriptionManager subscriptionManager;
    private final List<ExtdocProvider> providers;
    private final ExtdocPreferences preferences;

    private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.M_PRE_RETURNTYPE
            | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES
            | JavaElementLabels.M_EXCEPTIONS | JavaElementLabels.F_PRE_TYPE_SIGNATURE
            | JavaElementLabels.T_TYPE_PARAMETERS;

    private final JavaElementLabelProvider labelProvider = new JavaElementLabelProvider((int) LABEL_FLAGS);

    private SashForm sash;
    private ScrolledComposite scrollable;
    private Composite content;
    private TableViewer viewer;

    private boolean visible = true;

    private ExtdocProvider activeProvider;
    private JavaSelectionEvent activeSelection;

    @Inject
    public ExtdocView(final EventBus workspaceBus, final SubscriptionManager subscriptionManager,
            final List<ExtdocProvider> providers, final ExtdocPreferences preferences) {
        this.workspaceBus = workspaceBus;
        this.subscriptionManager = subscriptionManager;
        this.providers = providers;
        this.preferences = preferences;
    }

    @Override
    public void createPartControl(final Composite parent) {
        createSash(parent);
        createProviderOverview();
        createContentArea();
        addVisibilityListener();
        applyUiPreferences();
        workspaceBus.register(this);
    }

    private void createSash(final Composite parent) {
        sash = new SashForm(parent, SWT.SMOOTH);
        sash.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        sash.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                preferences.storeSashWeights(sash.getWeights());
            }
        });
    }

    private void createProviderOverview() {
        viewer = new TableViewer(sash);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((ExtdocProvider) element).getDescription().getName();
            }

            @Override
            public Image getImage(Object element) {
                return ((ExtdocProvider) element).getDescription().getImage();
            }
        });
        viewer.setInput(providers);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ExtdocProvider newProvider = RCPUtils.<ExtdocProvider> first(event.getSelection()).orNull();
                if (newProvider == activeProvider) {
                    return;
                }
                activeProvider = newProvider;
                Job job = new Job("Update Extdoc") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        onJavaSelection(activeSelection);
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem(true);
                job.schedule();
            }
        });
        viewer.setSelection(new StructuredSelection(Iterables.getFirst(providers, null)));
    }

    private void createContentArea() {
        scrollable = new ScrolledComposite(sash, SWT.H_SCROLL | SWT.V_SCROLL);
        scrollable.getVerticalBar().setIncrement(20);
        scrollable.setExpandHorizontal(true);
        scrollable.setExpandVertical(true);
        content = new Composite(scrollable, SWT.NONE);
        content.setLayout(new GridLayout());
        content.setFont(JFaceResources.getDialogFont());
        ExtdocUtils.setInfoBackgroundColor(content);

        scrollable.setContent(content);
    }

    private void applyUiPreferences() {
        sash.setWeights(preferences.loadSashWeights());
    }

    private void addVisibilityListener() {
        getViewSite().getPage().addPartListener(new PartListener2Adapter() {

            @Override
            public void partHidden(final IWorkbenchPartReference partRef) {
                if (isExtdocView(partRef)) {
                    visible = false;
                }
            }

            @Override
            public void partVisible(final IWorkbenchPartReference partRef) {
                if (isExtdocView(partRef)) {
                    visible = true;
                }
            }

            private boolean isExtdocView(final IWorkbenchPartReference partRef) {
                return partRef.getPart(false) == ExtdocView.this;
            }

        });
    }

    @Override
    public void dispose() {
        workspaceBus.unregister(this);
        super.dispose();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onJavaSelection(final JavaSelectionEvent selection) {
        this.activeSelection = selection;
        if (visible && activeProvider != null && activeSelection != null) {
            try {
                disposeOldContentAndDisableRedrawOnContentArea();
                runProvider(selection);
                refreshAndEnableDrawContentArea();
            } catch (Exception e) {
                RecommendersPlugin.logError(e, "Exception during view update." + selection);
            }
        }
    }

    private void runProvider(JavaSelectionEvent selection) throws IllegalAccessException, InvocationTargetException {
        Optional<Method> opt = subscriptionManager.findSubscribedMethod(activeProvider, selection);
        if (opt.isPresent()) {
            Method method = opt.get();
            IJavaElement element = selection.getElement();
            method.invoke(activeProvider, element, selection, content);
        }
    }

    private void refreshAndEnableDrawContentArea() {
        content.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                Point size = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                scrollable.setMinSize(size);
                content.layout();
                // content.setRedraw(true);
            }
        });
    }

    private void disposeOldContentAndDisableRedrawOnContentArea() {
        content.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                // content.setRedraw(false);
                ExtdocUtils.disposeChildren(content);
                addCurrentSelectionHeader();
            }
        });
    }

    private void addCurrentSelectionHeader() {
        final IJavaElement element = activeSelection.getElement();
        final String text;
        switch (element.getElementType()) {
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        case IJavaElement.PACKAGE_FRAGMENT:
            text = element.getElementName();
            break;
        case IJavaElement.LOCAL_VARIABLE:
            text =
                    JavaElementLabels.getElementLabel(element, JavaElementLabels.F_PRE_TYPE_SIGNATURE
                            | JavaElementLabels.F_POST_QUALIFIED);
            break;
        default:
            text = JavaElementLabels.getElementLabel(element, LABEL_FLAGS);
            break;
        }
        Composite header = new Composite(content, SWT.NONE);
        ExtdocUtils.setInfoBackgroundColor(header);
        header.setLayout(new GridLayout(2, false));

        Label img = new Label(header, SWT.NONE);
        img.setImage(labelProvider.getImage(element));
        setInfoForegroundColor(img);
        setInfoBackgroundColor(img);

        Label name = createLabel(header, text, true);
        name.setFont(JFaceResources.getHeaderFont());
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}