/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 *     Patrick Gottschaemmer, Olav Lenz - add Drag'n'Drop support
 *     Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.rcp.utils.PartListener2Adapter;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ApidocsView extends ViewPart {

    public static final String ID = "org.eclipse.recommenders.extdoc.rcp.ExtdocView"; //$NON-NLS-1$

    private Logger log = LoggerFactory.getLogger(getClass());
    private final EventBus workspaceBus;
    private final SubscriptionManager subscriptionManager;
    private final List<ApidocProvider> providers;
    private final ApidocsPreferences preferences;

    private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.M_PRE_RETURNTYPE
            | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES
            | JavaElementLabels.M_EXCEPTIONS | JavaElementLabels.F_PRE_TYPE_SIGNATURE
            | JavaElementLabels.T_TYPE_PARAMETERS;
    private static final int MOVE_AFTER = 1;
    private static final int MOVE_BEFORE = 0;

    private final JavaElementLabelProvider labelProvider = new JavaElementLabelProvider((int) LABEL_FLAGS);

    private SashForm sash;
    private ScrolledComposite scrollable;
    private Composite content;
    private TableViewer viewer;
    private List<ApidocProvider> providerRanking;

    private boolean visible = true;

    private ApidocProvider activeProvider;
    private JavaElementSelectionEvent activeSelection;

    @Inject
    public ApidocsView(final EventBus workspaceBus, final SubscriptionManager subscriptionManager,
            final List<ApidocProvider> providers, final ApidocsPreferences preferences) {
        this.workspaceBus = workspaceBus;
        this.subscriptionManager = subscriptionManager;
        this.providers = providers;
        this.preferences = preferences;
        providerRanking = loadProviderRanking();
    }

    private List<ApidocProvider> loadProviderRanking() {
        List<ApidocProvider> providerRanking = new LinkedList<ApidocProvider>();
        Map<String, ApidocProvider> providerMap = fillProviderMap();
        List<String> providerIds = preferences.loadOrderedProviderIds();
        LinkedList<ApidocProvider> remainingProviders = new LinkedList<ApidocProvider>(providers);

        for (String providerName : providerIds) {
            ApidocProvider tmpProvider = providerMap.get(providerName);
            if (tmpProvider != null) {
                providerRanking.add(tmpProvider);
                remainingProviders.remove(tmpProvider);
            }
        }
        providerRanking.addAll(remainingProviders);
        return providerRanking;
    }

    private HashMap<String, ApidocProvider> fillProviderMap() {
        HashMap<String, ApidocProvider> providerMap = new HashMap<String, ApidocProvider>();
        for (ApidocProvider provider : providers) {
            providerMap.put(provider.getId(), provider);
        }
        return providerMap;
    }

    @VisibleForTesting
    public void storeProviderRanking() {
        preferences.storeProviderRanking(providerRanking);
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
        viewer = new TableViewer(sash, SWT.SINGLE);

        addDnDSupport();

        viewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object first, Object second) {
                int indexFirst = providerRanking.indexOf(first);
                int indexSecond = providerRanking.indexOf(second);

                return indexFirst - indexSecond;
            }
        });

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((ApidocProvider) element).getDescription().getName();
            }

            @Override
            public Image getImage(Object element) {
                return ((ApidocProvider) element).getDescription().getImage();
            }
        });
        viewer.setInput(providers);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ApidocProvider newProvider = Selections.<ApidocProvider>getFirstSelected(event.getSelection()).orNull();
                if (newProvider == activeProvider) {
                    return;
                }
                activeProvider = newProvider;
                Job job = new Job(Messages.JOB_UPDATING_APIDOCS) {

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

    private void addDnDSupport() {
        final int operations = DND.DROP_MOVE;
        final Transfer[] transferTypes = new Transfer[] { DnDProviderTransfer.getInstance() };

        viewer.addDragSupport(operations, transferTypes, new DragSourceAdapter() {

            @Override
            public void dragSetData(final DragSourceEvent event) {
                final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

                if (DnDProviderTransfer.getInstance().isSupportedType(event.dataType)) {
                    final ApidocProvider selectedProvider = (ApidocProvider) selection.getFirstElement();
                    DnDProviderTransfer.getInstance().setExtdocProvider(selectedProvider);
                }
            }
        });

        viewer.addDropSupport(operations, transferTypes, new ViewerDropAdapter(viewer) {

            private int newIndex;
            private int currentFeedback;

            @Override
            public boolean validateDrop(final Object target, final int operation, final TransferData transferType) {
                return DnDProviderTransfer.getInstance().isSupportedType(transferType);
            }

            @Override
            public void dragOver(final DropTargetEvent event) {
                if (determineLocation(event) == ViewerDropAdapter.LOCATION_BEFORE && isFirstProvider(event)) {
                    event.feedback = DND.FEEDBACK_INSERT_BEFORE;
                } else {
                    event.feedback = DND.FEEDBACK_INSERT_AFTER;
                }
                currentFeedback = event.feedback;
            }

            private boolean isFirstProvider(final DropTargetEvent event) {
                return providerRanking.indexOf(determineTarget(event)) == 0;
            }

            @Override
            public void drop(final DropTargetEvent event) {
                if (event.item != null) {
                    newIndex = providerRanking.indexOf(event.item.getData());
                } else {
                    newIndex = providerRanking.size() - 1;
                }
                performDrop(event.data);
            }

            @Override
            public boolean performDrop(final Object data) {
                final ApidocProvider provider = (ApidocProvider) data;
                final int oldIndex = providerRanking.indexOf(provider);

                if (currentFeedback == DND.FEEDBACK_INSERT_AFTER) {
                    moveAfter(oldIndex, newIndex);
                } else {
                    moveBefore(oldIndex, newIndex);
                }
                storeProviderRanking();
                viewer.refresh();
                return true;
            }
        });
    }

    private void move(int oldIndex, int newIndex, int moveStyle) {
        if (newIndex == oldIndex) {
            return;
        } else if (newIndex < oldIndex) {
            ApidocProvider tmp = providerRanking.remove(oldIndex);
            providerRanking.add(newIndex + moveStyle, tmp);
        } else {
            ApidocProvider tmp = providerRanking.remove(oldIndex);
            providerRanking.add(newIndex - 1 + moveStyle, tmp);
        }
    }

    @VisibleForTesting
    public void moveAfter(int oldIndex, int newIndex) {
        move(oldIndex, newIndex, MOVE_AFTER);
    }

    @VisibleForTesting
    public void moveBefore(int oldIndex, int newIndex) {
        move(oldIndex, newIndex, MOVE_BEFORE);
    }

    @VisibleForTesting
    public List<ApidocProvider> getProviderRanking() {
        return providerRanking;
    }

    private void createContentArea() {
        scrollable = new ScrolledComposite(sash, SWT.H_SCROLL | SWT.V_SCROLL);
        scrollable.getVerticalBar().setIncrement(20);
        scrollable.setExpandHorizontal(true);
        scrollable.setExpandVertical(true);
        content = new Composite(scrollable, SWT.NONE);
        content.setLayout(new GridLayout());
        content.setFont(JFaceResources.getDialogFont());
        ApidocsViewUtils.setInfoBackgroundColor(content);

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
                return partRef.getPart(false) == ApidocsView.this;
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
    public void onJavaSelection(final JavaElementSelectionEvent selection) {
        activeSelection = selection;
        if (visible && activeProvider != null && activeSelection != null) {
            try {
                disposeOldContentAndDisableRedrawOnContentArea();
                runProvider(selection);
                refreshAndEnableDrawContentArea();
            } catch (Exception e) {
                log.error("Exception during view update." + selection, e); //$NON-NLS-1$
            }
        }
    }

    private void runProvider(JavaElementSelectionEvent selection) throws IllegalAccessException,
            InvocationTargetException {
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
                ApidocsViewUtils.disposeChildren(content);
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
            text = JavaElementLabels.getElementLabel(element, JavaElementLabels.F_PRE_TYPE_SIGNATURE
                    | JavaElementLabels.F_POST_QUALIFIED);
            break;
        default:
            text = JavaElementLabels.getElementLabel(element, LABEL_FLAGS);
            break;
        }
        Composite header = new Composite(content, SWT.NONE);
        ApidocsViewUtils.setInfoBackgroundColor(header);
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
