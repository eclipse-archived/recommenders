/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.util.Collection;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ModelArchiveCoordinatesView extends ViewPart {

    private Table table;

    @Inject
    IModelIndex index;

    @Inject
    EventBus bus;

    private TableViewer tableViewer;
    private Multimap<String, String> models;

    @Override
    public void createPartControl(Composite parent) {
        bus.register(this);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(container, SWT.NONE);
        TableColumnLayout tableLayout = new TableColumnLayout();
        composite.setLayout(tableLayout);

        tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn tvcPrimary = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tcPrimary = tvcPrimary.getColumn();
        tableLayout.setColumnData(tcPrimary, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
        tcPrimary.setText("Primary");
        tvcPrimary.setLabelProvider(new ColumnLabelProvider());

        newColumn(tableLayout, Constants.CLASS_CALL_MODELS);
        newColumn(tableLayout, Constants.CLASS_OVRD_MODEL);
        newColumn(tableLayout, Constants.CLASS_OVRP_MODEL);
        newColumn(tableLayout, Constants.CLASS_OVRM_MODEL);
        newColumn(tableLayout, Constants.CLASS_SELFC_MODEL);
        newColumn(tableLayout, Constants.CLASS_SELFM_MODEL);

        tableViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return ((Multimap<?, ?>) inputElement).keySet().toArray();
            }
        });
        tableViewer.setSorter(new ViewerSorter());
        createActions();
        initializeToolBar();
        initializeMenu();
        initializeContent();
    }

    private void newColumn(TableColumnLayout tableLayout, final String classifier) {
        TableViewerColumn tvcSelfc = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tcSelfc = tvcSelfc.getColumn();
        tcSelfc.setMoveable(true);
        tcSelfc.setResizable(false);
        tableLayout.setColumnData(tcSelfc, new ColumnPixelData(10, false, true));
        tcSelfc.setText(classifier);
        tvcSelfc.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Collection<String> values = models.get((String) element);
                return values.contains(classifier) ? "+" : "-";
            }
        });
    }

    private void initializeContent() {
        models = LinkedListMultimap.create();
        addClassifierToIndex(models, Constants.CLASS_CALL_MODELS);
        addClassifierToIndex(models, Constants.CLASS_OVRD_MODEL);
        addClassifierToIndex(models, Constants.CLASS_OVRM_MODEL);
        addClassifierToIndex(models, Constants.CLASS_OVRP_MODEL);
        addClassifierToIndex(models, Constants.CLASS_SELFC_MODEL);
        addClassifierToIndex(models, Constants.CLASS_SELFM_MODEL);
        tableViewer.setInput(models);
    }

    private void addClassifierToIndex(Multimap<String, String> models, String classifier) {
        for (ModelCoordinate mc : index.getKnownModels(classifier)) {
            String key = Joiner.on(":").join(mc.getGroupId(), mc.getArtifactId(), mc.getVersion());
            models.put(key, classifier);
        }
    }

    private void createActions() {
        // Create the actions
    }

    private void initializeToolBar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    }

    private void initializeMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    @Subscribe
    public void onModelIndexOpened(ModelIndexOpenedEvent e) {
        new UIJob("Refreshing Model Index View...") {
            {
                schedule();
            }

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                initializeContent();
                return Status.OK_STATUS;
            }
        };
    }

    @Override
    public void dispose() {
        super.dispose();
        bus.unregister(this);
    }
}
