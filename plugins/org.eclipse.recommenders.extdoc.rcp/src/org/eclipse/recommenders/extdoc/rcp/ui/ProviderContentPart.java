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
package org.eclipse.recommenders.extdoc.rcp.ui;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.extdoc.rcp.ExtdocPlugin;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderSelectionEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class ProviderContentPart {

    private final List<Provider> providers;

    private Composite stack;
    private StackLayout stackLayout;

    private Composite container;
    private Composite visiblePanel;
    private Composite renderingPanel;
    private ScrolledComposite scrollingComposite;
    private Composite scrolledContent;

    private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.M_PRE_RETURNTYPE
            | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES
            | JavaElementLabels.M_EXCEPTIONS | JavaElementLabels.F_PRE_TYPE_SIGNATURE
            | JavaElementLabels.T_TYPE_PARAMETERS;

    private Map<Provider, ProviderArea> providerAreas;
    private CLabel selectionStatus;
    private JavaElementLabelProvider labelProvider;

    private GridLayoutFactory defaultGridLayoutFactory = GridLayoutFactory.fillDefaults().spacing(0, 0);
    private GridDataFactory defaultGridDataFactory = GridDataFactory.fillDefaults().grab(true, true);

    @Inject
    public ProviderContentPart(List<Provider> providers) {
        this.providers = providers;

        providerAreas = Maps.newHashMap();
        labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    }

    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NO_BACKGROUND);
        container.setLayout(defaultGridLayoutFactory.create());

        createScrollingComposite();
        createScrolledContent();
        createStack();

        createWaitingScreen();
        createNewRenderingPanel();

        moveOnTop(visiblePanel);
    }

    private void moveOnTop(Composite newTopComposite) {
        stackLayout.topControl = newTopComposite;
        stack.layout();
    }

    private void createScrollingComposite() {
        scrollingComposite = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL);
        scrollingComposite.setLayout(defaultGridLayoutFactory.create());
        scrollingComposite.setLayoutData(defaultGridDataFactory.create());
        scrollingComposite.setExpandVertical(true);
        scrollingComposite.setExpandHorizontal(true);
    }

    private void createScrolledContent() {
        scrolledContent = new Composite(scrollingComposite, SWT.NONE);
        scrolledContent.setLayout(defaultGridLayoutFactory.create());

        scrolledContent.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // handle window or view resize properly
                Point size = scrolledContent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
                scrollingComposite.setMinSize(size);
            }
        });

        scrollingComposite.setContent(scrolledContent);
    }

    private void createStack() {
        stack = new Composite(scrolledContent, SWT.NONE);
        stack.setLayoutData(defaultGridDataFactory.create());
        stackLayout = new StackLayout();
        stack.setLayout(stackLayout);
    }

    private void createWaitingScreen() {
        visiblePanel = new Composite(stack, SWT.NONE);
        visiblePanel.setLayout(new FillLayout());
        Label l = new Label(visiblePanel, SWT.NONE);
        l.setText("waiting for selection...");
    }

    public Composite getRenderingArea(Provider p) {
        return providerAreas.get(p).getContentArea();
    }

    public void createNewRenderingPanel() {
        renderingPanel = new Composite(stack, SWT.NONE);
        renderingPanel.setBackground(renderingPanel.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        renderingPanel.setLayout(defaultGridLayoutFactory.create());

        createSelectionInfoArea();

        for (Provider p : providers) {
            ProviderArea providerArea = new ProviderArea(p);
            providerArea.createControl(renderingPanel);
            providerAreas.put(p, providerArea);
        }

        renderingPanel.layout();
    }

    private void createSelectionInfoArea() {
        Composite selectionArea = new Composite(renderingPanel, SWT.NO_BACKGROUND);
        selectionArea.setLayout(defaultGridLayoutFactory.create());
        selectionArea.setLayoutData(defaultGridDataFactory.grab(true, false).create());

        selectionStatus = new CLabel(selectionArea, SWT.NO_BACKGROUND);
        selectionStatus.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        selectionStatus.setBackground(selectionArea.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    @Subscribe
    public void handle(NewSelectionEvent e) {
        updateSelectionStatus(e.selection);
    }

    private void updateSelectionStatus(JavaSelectionEvent selection) {
        IJavaElement element = selection.getElement();
        String text = JavaElementLinks.getElementLabel(element, LABEL_FLAGS);
        text = stripHtml(text);

        selectionStatus.setText(text);
        selectionStatus.setImage(labelProvider.getImage(element));
    }

    private String stripHtml(String text) {
        return text.replaceAll("\\<.*?\\>", "");
    }

    @Subscribe
    public void handle(RenderNowEvent e) {
        makeRenderingPanelVisible();
        relayout();
        scrollToTop();
    }

    private void scrollToTop() {
        scrollingComposite.setOrigin(0, 0);
    }

    private void makeRenderingPanelVisible() {
        renderingPanel.layout();
        moveOnTop(renderingPanel);
        visiblePanel.dispose();
        visiblePanel = renderingPanel;

        container.layout();
    }

    private void relayout() {

        // TODO kann das was rausfliegen?
        scrolledContent.notifyListeners(SWT.RESIZE, new Event());
        container.layout();
        selectionStatus.getParent().layout();
        scrollingComposite.layout();
        scrolledContent.layout();

    }

    @Subscribe
    public void handle(ProviderNotAvailableEvent e) {
        ProviderArea area = providerAreas.get(e.provider);
        area.hide();
    }

    @Subscribe
    public void handle(ProviderSelectionEvent e) {
        ProviderArea area = providerAreas.get(e.provider);
        scrollingComposite.setOrigin(area.getLocation());
    }

    @Subscribe
    public void handle(final ProviderFinishedEvent e) {
        ProviderArea area = providerAreas.get(e.provider);
        area.showContent();
        visiblePanel.layout();
    }

    @Subscribe
    public void handle(ProviderDelayedEvent e) {
        ProviderArea area = providerAreas.get(e.provider);
        area.setStatus("provider is delayed...");
        area.showStatus();
        visiblePanel.layout();
    }

    // @Subscribe
    // public void handle(ProviderStartedOnActivationEvent e) {
    // ProviderArea area = providerAreas.get(e.provider);
    // area.setStatus("manually activated...");
    // area.showStatus();
    // visiblePanel.layout();
    // }

    @Subscribe
    public void handle(ProviderFinishedLateEvent e) {
        final ProviderArea area = providerAreas.get(e.provider);
        String statusMessage = "provider finished late <a>show</a>";
        area.setStatusWithCallback(statusMessage, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                area.showContent();
                visiblePanel.layout();
            }
        });
        area.showStatus();
        visiblePanel.layout();

        relayout();
    }

    @Subscribe
    public void handle(final ProviderFailedEvent e) {
        final ProviderArea area = providerAreas.get(e.provider);
        String statusMessage = "provider failed <a>show exception</a>";
        area.setStatusWithCallback(statusMessage, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String stackTrace = StringUtils.join(e.exception.getStackTrace(), '\n');
                String errorMessage = e.exception.getMessage();
                Status status = new Status(IStatus.ERROR, ExtdocPlugin.PLUGIN_ID, errorMessage);
                ErrorDialog.openError(container.getShell(), errorMessage, stackTrace, status);
            }
        });
        area.showStatus();
        visiblePanel.layout();
    }

    @Subscribe
    public void handle(ProviderOrderChangedEvent e) {
        System.out.printf("order changed: %d, %d", e.oldIndex, e.newIndex);

        ProviderArea areaToMove = providerAreas.get(e.provider);
        ProviderArea areaRef = providerAreas.get(e.reference);

        boolean isAbove = e.oldIndex > e.newIndex;
        if (isAbove) {
            areaToMove.moveAbove(areaRef);
        } else {
            areaToMove.moveBelow(areaRef);
        }

        visiblePanel.layout();
    }

    @Subscribe
    public void handle(ProviderDeactivationEvent e) {
        System.out.println("cleaning up provider area after deactivation...");
        ProviderArea area = providerAreas.get(e.provider);
        area.hide();
        area.cleanup();
        area.layout();
        visiblePanel.layout();
    }
}