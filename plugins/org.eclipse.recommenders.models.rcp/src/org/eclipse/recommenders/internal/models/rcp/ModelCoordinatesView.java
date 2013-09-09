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

import static org.eclipse.recommenders.rcp.SharedImages.ELCL_REFRESH;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.models.Coordinates;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ModelCoordinatesView extends ViewPart {

    private DataBindingContext m_bindingContext;

    private Table table;

    @Inject
    IModelIndex index;

    @Inject
    SharedImages images;

    @Inject
    EclipseModelRepository repo;

    @Inject
    EventBus bus;

    private TableViewer tableViewer;
    private Multimap<ProjectCoordinate, String> models;

    private Text txtSearch;

    @Override
    public void createPartControl(Composite parent) {
        bus.register(this);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        txtSearch = new Text(container, SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH | SWT.CANCEL);
        txtSearch.setMessage("type filter text");
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN && table.getItemCount() != 0) {
                    table.setFocus();
                    table.setSelection(0);
                }
            }
        });

        Composite composite = new Composite(container, SWT.NONE);

        TableColumnLayout tableLayout = new TableColumnLayout();
        composite.setLayout(tableLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

        tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        ColumnViewerToolTipSupport.enableFor(tableViewer);
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
        initializeContent();
        addRefreshButton();
        addDeleteButton();
        addContextMenu();
        m_bindingContext = initDataBindings();
    }

    private void addRefreshButton() {
        IAction refreshAction = new Action() {
            @Override
            public void run() {
                refreshData();
            }
        };
        refreshAction.setToolTipText("Refresh");
        refreshAction.setImageDescriptor(images.getDescriptor(ELCL_REFRESH));
        getViewSite().getActionBars().getToolBarManager().add(refreshAction);
    }

    private void addDeleteButton() {
        IAction deleteAction = new Action() {

            @Override
            public void run() {
                deleteCacheAndRefresh();
            }
        };
        deleteAction.setText("Delete models");
        deleteAction.setImageDescriptor(images.getDescriptor(SharedImages.ELCL_DELETE));
        getViewSite().getActionBars().getMenuManager().add(deleteAction);
    }

    private void addContextMenu() {
        final MenuManager menuManager = new MenuManager();
        Menu contextMenu = menuManager.createContextMenu(table);
        menuManager.setRemoveAllWhenShown(true);
        table.setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                Set<ProjectCoordinate> pcs = Selections.toSet(tableViewer.getSelection());
                if (!pcs.isEmpty()) {
                    menuManager.add(new TriggerModelDownloadActionForProjectCoordinates("Download models", pcs, index,
                            repo, bus));
                }
            }
        });
    }

    private void newColumn(TableColumnLayout tableLayout, final String classifier) {
        TableViewerColumn tvColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
        TableColumn column = tvColumn.getColumn();
        column.setMoveable(true);
        column.setResizable(false);
        tableLayout.setColumnData(column, new ColumnPixelData(20, false, true));
        column.setText(classifier.toUpperCase());
        tvColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return null;
            }

            @Override
            public String getToolTipText(Object element) {
                ProjectCoordinate pc = (ProjectCoordinate) element;
                ModelCoordinate mc = Coordinates.toModelCoordinate(pc, classifier, "zip");
                if (!containsModel(classifier, element)) {
                    return "No model registered";
                } else if (isDownloaded(mc)) {
                    return "Locally available";
                } else {
                    return "Remotely available";
                }
            }

            private boolean containsModel(final String classifier, Object element) {
                Collection<String> values = models.get((ProjectCoordinate) element);
                boolean contains = values.contains(classifier);
                return contains;
            }

            @Override
            public Image getImage(Object element) {
                ProjectCoordinate pc = (ProjectCoordinate) element;
                ModelCoordinate mc = Coordinates.toModelCoordinate(pc, classifier, "zip");
                if (!containsModel(classifier, element)) {
                    return images.getImage(SharedImages.OBJ_CROSS_RED);
                } else if (!isDownloaded(mc)) {
                    return images.getImage(SharedImages.OBJ_BULLET_BLUE);
                } else {
                    return images.getImage(SharedImages.OBJ_CHECK_GREEN);
                }
            }

            private boolean isDownloaded(final ModelCoordinate mc) {
                return repo.getLocation(mc, false).isPresent();
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

    private void addClassifierToIndex(Multimap<ProjectCoordinate, String> models, String classifier) {
        for (ModelCoordinate mc : index.getKnownModels(classifier)) {
            ProjectCoordinate pc = Coordinates.toProjectCoordinate(mc);
            models.put(pc, classifier);
        }
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    // needs to be public to work with PojoProperties
    public void setFilter(final String filter) {
        tableViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                ProjectCoordinate coord = (ProjectCoordinate) element;
                for (String s : coord.toString().split(":")) {
                    if (s.startsWith(filter)) {
                        return true;
                    }
                }
                return false;
            }
        }

        });
    }

    @Override
    public void dispose() {
        super.dispose();
        m_bindingContext.dispose();
        bus.unregister(this);
    }

    @Subscribe
    public void onModelIndexOpened(ModelIndexOpenedEvent e) {
        refreshData();
    }

    @Subscribe
    public void onModelArchiveDownloaded(final ModelArchiveDownloadedEvent e) {
        new UIJob("") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                ProjectCoordinate pc = Coordinates.toProjectCoordinate(e.model);
                tableViewer.refresh(pc);
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private void refreshData() {
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

    private void deleteCacheAndRefresh() {
        new Job("Deleting model cache...") {
            {
                schedule();
            }

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    repo.deleteModels();
                    // TODO Would be nice to have something like schule(job1).then(job2), rather than having the first
                    // job schedule the second.
                    refreshData();
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(Status.ERROR, org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID,
                            "Failed to delete model cache");
                }
            }
        };
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        IObservableValue search = WidgetProperties.text(SWT.Modify).observeDelayed(400, txtSearch);
        IObservableValue filter = PojoProperties.value("filter").observe(this);
        bindingContext.bindValue(filter, search, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        return bindingContext;
    }
}
