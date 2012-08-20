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

import static java.lang.String.format;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoBackgroundColor;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.setInfoForegroundColor;
import static org.eclipse.recommenders.rcp.RecommendersPlugin.logWarning;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderStatusEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocPlugin;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ProviderContentPart {

    private final List<ExtdocProvider> providers;

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

    // using class instead of instances to be sure that only one provider of a given type exists. We have rendering
    // issues and this is used as an additional check:
    private Map<ExtdocProvider, ProviderArea> providerAreas;
    private CLabel selectionStatus;
    private final JavaElementLabelProvider labelProvider;

    private final GridLayoutFactory defaultGridLayoutFactory = GridLayoutFactory.fillDefaults().spacing(0, 0);
    private final GridDataFactory defaultGridDataFactory = GridDataFactory.fillDefaults().grab(true, true);

    private HashMultimap<Class<? extends ExtdocProvider>, Class<?>> providerEvents;

    @Inject
    public ProviderContentPart(final List<ExtdocProvider> providers) {
        this.providers = ImmutableList.copyOf(providers);
        this.labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    }

    public void createControl(final Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(defaultGridLayoutFactory.create());

        createScrollingComposite();
        createScrolledContent();
        createStack();

        createWaitingScreen();
        createNewRenderingPanel();

        stackLayout.topControl = visiblePanel;
    }

    private void createScrollingComposite() {
        scrollingComposite = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL);
        scrollingComposite.setLayout(defaultGridLayoutFactory.create());
        scrollingComposite.setLayoutData(defaultGridDataFactory.create());
        scrollingComposite.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        scrollingComposite.setExpandVertical(true);
        scrollingComposite.setExpandHorizontal(true);
        scrollingComposite.getVerticalBar().setIncrement(50);
        scrollingComposite.getHorizontalBar().setIncrement(50);

    }

    private void createScrolledContent() {
        scrolledContent = new Composite(scrollingComposite, SWT.NONE);
        scrolledContent.setLayout(defaultGridLayoutFactory.create());

        scrolledContent.addListener(SWT.Resize, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                resizeScrolledComposite();
            }
        });

        scrollingComposite.setContent(scrolledContent);
    }

    private void resizeScrolledComposite() {
        final int newWidth = scrolledContent.getSize().x;
        final Point newSize = scrolledContent.computeSize(newWidth, SWT.DEFAULT);
        scrollingComposite.setMinHeight(newSize.y);
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
        visiblePanel.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        final Label l = new Label(visiblePanel, SWT.NONE);
        l.setText("waiting for selection...");
    }

    public void setFocus() {
        visiblePanel.setFocus();
    }

    public Composite getRenderingArea(final ExtdocProvider p) {
        return providerAreas.get(p).getContentArea();
    }

    public void createNewRenderingPanel() {
        renderingPanel = new Composite(stack, SWT.NONE);
        renderingPanel.setRedraw(false);
        renderingPanel.setBackground(renderingPanel.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        renderingPanel.setLayout(defaultGridLayoutFactory.create());

        createSelectionInfoArea();
        providerAreas = Maps.newHashMap();
        providerEvents = HashMultimap.create();

        for (final ExtdocProvider p : providers) {
            final ProviderArea providerArea = new ProviderArea(p);
            providerArea.createControl(renderingPanel);
            providerAreas.put(p, providerArea);
        }

        // renderingPanel.layout();
    }

    private void createSelectionInfoArea() {
        final Composite selectionArea = new Composite(renderingPanel, SWT.NONE);
        setInfoBackgroundColor(selectionArea);

        selectionArea.setLayout(defaultGridLayoutFactory.create());
        selectionArea.setLayoutData(defaultGridDataFactory.grab(true, false).create());

        selectionStatus = new CLabel(selectionArea, SWT.NONE);
        setInfoBackgroundColor(selectionStatus);
        setInfoForegroundColor(selectionArea);
        selectionStatus.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        selectionStatus.setBackground(selectionArea.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    @Subscribe
    public void onEvent(final NewSelectionEvent e) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                updateSelectionStatus(e.selection);
            }
        });
    }

    private void updateSelectionStatus(final JavaSelectionEvent selection) {
        final IJavaElement element = selection.getElement();

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
        selectionStatus.setText(text);
        selectionStatus.setImage(labelProvider.getImage(element));
    }

    @Subscribe
    public void onEvent(final RenderNowEvent e) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                makeRenderingPanelVisible();
                relayout();
                scrollToTop();
                renderingPanel.setRedraw(true);
            }
        });
    }

    private void scrollToTop() {
        scrollingComposite.setOrigin(0, 0);
    }

    private void makeRenderingPanelVisible() {
        stackLayout.topControl = renderingPanel;
        visiblePanel.dispose();
        visiblePanel = renderingPanel;
    }

    private void relayout() {
        resizeScrolledComposite();
        stack.layout();
    }

    @Subscribe
    public void onEvent(final ProviderNotAvailableEvent e) {
        recordProviderEvent(e);
        asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);

                if (e.hasFinishedLate) {
                    area.setStatus("provider finished without data");
                    area.showStatus();
                } else {
                    area.hide();
                }
            }
        });
    }

    private void asyncExec(final Runnable runnable) {
        container.getDisplay().asyncExec(runnable);
    }

    @Subscribe
    public void onEvent(final ProviderSelectionEvent e) {
        recordProviderEvent(e);

        asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);
                scrollingComposite.setOrigin(area.getLocation());
            }
        });
    }

    @Subscribe
    public void onEvent(final ProviderFinishedEvent e) {
        if (hasSentFinishedEventBefore(e.provider)) {
            return;
        }
        recordProviderEvent(e);

        asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);
                area.showContent();

            }
        });
    }

    @Subscribe
    public void onEvent(final ProviderDelayedEvent e) {
        if (isProviderFinishedAlready(e)) {
            return;
        }
        recordProviderEvent(e);
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);
                area.setStatus("provider is delayed...");
                area.showStatus();
            }
        });
    }

    private boolean isProviderFinishedAlready(final ProviderDelayedEvent e) {
        // due to concurrency there might be milliseconds that may cause a layout glitch: a provider rendered the
        // contents already but the scheduler finds this provider to be failed. This is checked as handled here.
        // TODO: is there a better way to handle this in the scheduler?
        final Class<?>[] finishedEvents = { ProviderFinishedEvent.class, ProviderFinishedLateEvent.class,
                ProviderFailedEvent.class, ProviderNotAvailableEvent.class };
        return providerEventsContainsAnyOf(e.provider, finishedEvents);
    }

    private boolean providerEventsContainsAnyOf(final ExtdocProvider provider, final Class<?>... events) {
        for (final Class<?> e : events) {
            if (providerEvents.containsEntry(provider.getClass(), e)) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onEvent(final ProviderFinishedLateEvent e) {
        if (hasSentFinishedEventBefore(e.provider)) {
            return;
        }
        recordProviderEvent(e);

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);
                final String statusMessage = "provider finished late <a>show</a>";
                area.setStatusWithCallback(statusMessage, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        area.showContent();
                        relayout();
                    }
                });
                area.showStatus();
                area.layout();
            }
        });
    }

    private void recordProviderEvent(final ProviderStatusEvent e) {
        providerEvents.put(e.provider.getClass(), e.getClass());
    }

    private boolean hasSentFinishedEventBefore(final ExtdocProvider provider) {
        if (providerEventsContainsAnyOf(provider, ProviderFinishedEvent.class, ProviderFinishedLateEvent.class,
                ProviderNotAvailableEvent.class)) {
            logWarning(
                    "Provider %s send 'ProviderFinishedLateEvent' but send other finished events before. Rejecting further paint events.",
                    provider.getClass());
            return true;
        }
        return false;
    }

    @Subscribe
    public void onEvent(final ProviderFailedEvent e) {
        recordProviderEvent(e);

        asyncExec(new Runnable() {

            @Override
            public void run() {

                final ProviderArea area = providerAreas.get(e.provider);
                final String statusMessage = "provider failed <a>show exception</a>";
                area.setStatusWithCallback(statusMessage, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent event) {
                        // REVIEW: TODO: printing the potentially LARGE stacktrace
                        // looked odd... details view of ErrorDialog is
                        // odd too. What to do?
                        // final String details =
                        // Throwables.getStackTraceAsString(e.exception);
                        final String providerName = e.provider.getDescription().getName();

                        final String dialogTitle = format("Extdoc Provider '%s' Failure", providerName);
                        final String errorMessage = findFirstNonNullErrorMessage(e.throwable);
                        final String rootCauseMessage = Throwables.getRootCause(e.throwable).getMessage();

                        final Status status = new Status(IStatus.ERROR, ExtdocPlugin.PLUGIN_ID, errorMessage,
                                e.throwable);
                        final String message = format(
                                "Provider %s failed with the following message: %s - %s\n\nSee error log for more details.",
                                providerName, errorMessage, rootCauseMessage);
                        RecommendersPlugin.logError(new Exception(e.throwable), message);
                        ErrorDialog.openError(container.getShell(), dialogTitle, message, status);
                    }

                    private String findFirstNonNullErrorMessage(final Throwable exception) {
                        final List<Throwable> causalChain = Throwables.getCausalChain(exception);
                        String errorMessage = null;
                        for (final Throwable t : causalChain) {
                            errorMessage = t.getMessage();
                            if (errorMessage != null) {
                                break;
                            }
                        }
                        return errorMessage;
                    }
                });
                area.showStatus();
            }
        });
    }

    @Subscribe
    public void onEvent(final ProviderOrderChangedEvent e) {
        asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea areaToMove = providerAreas.get(e.provider);
                final ProviderArea areaRef = providerAreas.get(e.reference);

                final boolean isAbove = e.oldIndex > e.newIndex;
                if (isAbove) {
                    areaToMove.moveAbove(areaRef);
                } else {
                    areaToMove.moveBelow(areaRef);
                }
                relayout();
            }
        });
    }

    @Subscribe
    public void onEvent(final ProviderDeactivationEvent e) {
        asyncExec(new Runnable() {

            @Override
            public void run() {
                final ProviderArea area = providerAreas.get(e.provider);
                area.hide();
                area.cleanup();
                area.layout();
                relayout();
            }
        });
    }
}