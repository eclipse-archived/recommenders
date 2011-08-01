/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.preferences;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DependenciesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final int MIN_WIDTH_TABLE = 200;

    private final ClasspathDependencyStore dependencyStore;
    private DependencyDetailsSection dependencyDetailsSection;
    private ModelDetailsSection modelDetailsSection;

    @Inject
    public DependenciesPreferencePage(final ClasspathDependencyStore dependencyStore) {
        this.dependencyStore = dependencyStore;
        noDefaultAndApplyButton();
    }

    @Override
    public void init(final IWorkbench workbench) {

    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite container = new Composite(parent, SWT.FILL);
        container.setLayout(new GridLayout(2, true));

        createTable(container);
        createDetailsSection(container);

        return container;
    }

    private void createTable(final Composite container) {
        final Composite tableComposite = new Composite(container, SWT.FILL);
        final TableViewer tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        final Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        setGridData(tableComposite, MIN_WIDTH_TABLE);
        tableComposite.setLayout(tableColumnLayout);

        createColumns(tableViewer, tableColumnLayout);

        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setInput(dependencyStore.getFiles());
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();
                if (selection.isEmpty()) {
                    selectFile(null);
                } else {
                    selectFile((File) ((StructuredSelection) selection).getFirstElement());
                }
            }
        });
        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object e1, final Object e2) {
                final File file1 = (File) e1;
                final File file2 = (File) e2;
                return file1.getName().compareTo(file2.getName());
            }
        });
    }

    private void createColumns(final TableViewer tableViewer, final TableColumnLayout tableColumnLayout) {
        final Image versionImage = loadImage("/icons/obj16/file_version.png");
        final Image versionUnknownImage = loadImage("/icons/obj16/file_version_unknown.png");
        final Image modelImage = loadImage("/icons/obj16/model.png");
        final Image modelUnknownImage = loadImage("/icons/obj16/model_unknown.png");

        ColumnViewerToolTipSupport.enableFor(tableViewer);
        TableViewerColumn column = createTableViewerColumn(tableViewer, "File", 200, 0);
        column.getColumn().setResizable(false);
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(final Object element) {
                return ((File) element).getName();
            }

            @Override
            public String getToolTipText(final Object element) {
                return ((File) element).getAbsolutePath();
            }
        });

        column = createTableViewerColumn(tableViewer, "", 20, 1);
        column.getColumn().setResizable(false);
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnPixelData(20));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(final Object element) {
                return hasDependencyInformation((File) element) ? versionImage : versionUnknownImage;
            }

            @Override
            public String getToolTipText(final Object element) {
                return hasDependencyInformation((File) element) ? "Name and version of dependency is known"
                        : "Some details are unknown for this dependency";
            }

            private boolean hasDependencyInformation(final File file) {
                if (dependencyStore.containsClasspathDependencyInfo(file)) {
                    final ClasspathDependencyInformation dependencyInfo = dependencyStore
                            .getClasspathDependencyInfo(file);
                    if (!dependencyInfo.symbolicName.isEmpty() && !dependencyInfo.version.isUnknown()) {
                        return true;
                    }
                }
                return false;
            }
        });

        column = createTableViewerColumn(tableViewer, "", 20, 2);
        column.getColumn().setResizable(false);
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnPixelData(20));
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(final Object element) {
                return hasModel((File) element) ? modelImage : modelUnknownImage;
            }

            @Override
            public String getToolTipText(final Object element) {
                return hasModel((File) element) ? "Model for this dependency is available"
                        : "No model for this dependency";
            }

            private boolean hasModel(final File file) {
                return dependencyStore.containsManifest(file);
            }
        });
    }

    private Image loadImage(final String name) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.recommenders.rcp.codecompletion.calls", name)
                .createImage();
    }

    private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int bound,
            final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
    }

    private void createDetailsSection(final Composite container) {
        final Composite detailsSection = new Composite(container, SWT.NONE);
        setGridData(detailsSection, 100);
        detailsSection.setLayout(new GridLayout(1, true));

        dependencyDetailsSection = new DependencyDetailsSection(detailsSection, dependencyStore);
        modelDetailsSection = new ModelDetailsSection(detailsSection, dependencyStore);
    }

    private void setGridData(final Control control, final int minimumWidth) {
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumWidth = minimumWidth;
        gridData.heightHint = 400;
        control.setLayoutData(gridData);
    }

    private void selectFile(final File file) {
        dependencyDetailsSection.selectFile(file);
        modelDetailsSection.selectFile(file);
    }

}
