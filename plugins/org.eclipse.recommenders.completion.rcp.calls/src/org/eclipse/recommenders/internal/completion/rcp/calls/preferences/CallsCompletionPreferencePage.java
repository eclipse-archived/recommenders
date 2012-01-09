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
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionPlugin.PLUGIN_ID;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.recommenders.internal.completion.rcp.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.ModelArchiveStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store.jobs.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionPlugin;
import org.eclipse.recommenders.utils.rcp.ScaleOneDimensionLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CallsCompletionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final int MIN_WIDTH_DETAILS_SECTION = 100;
    private static final int MIN_WIDTH_TABLE = 200;

    private final ClasspathDependencyStore dependencyStore;
    private DependencyDetailsSection dependencyDetailsSection;
    private ModelDetailsSection modelDetailsSection;
    private CommandSection commandSection;
    private final RemoteResolverJobFactory jobFactory;
    private final ModelArchiveStore archiveStore;
    private Text webserviceBaseurl;

    @Inject
    public CallsCompletionPreferencePage(final ClasspathDependencyStore dependencyStore,
            final RemoteResolverJobFactory jobFactory, final ModelArchiveStore archiveStore) {
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;
        this.archiveStore = archiveStore;
        noDefaultAndApplyButton();
        setDescription("All dependencies of your open and Recommenders enabled projects are listed below. "
                + "Select an entry to edit the name and version of a dependency. "
                + "Models will be selected by these details.");
    }

    @Override
    public void init(final IWorkbench workbench) {
        final IPreferenceStore store = CallsCompletionPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    @Override
    protected Control createContents(final Composite parent) {
        createWebserviceBaseurlSection(parent);
        final Composite container = createTwoColumnContainer(parent);
        final Composite tableWrapper = createTableWrapper(container);
        createTable(tableWrapper);
        createDetailsSection(container);

        return container;
    }

    private void createWebserviceBaseurlSection(final Composite parent) {
        final GridDataFactory factory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false);
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(factory.create());
        final Label label = new Label(container, SWT.NONE);
        label.setText("Remote URL:");
        webserviceBaseurl = new Text(container, SWT.BORDER);
        webserviceBaseurl.setLayoutData(factory.create());
        webserviceBaseurl.setText(getPreferenceStore().getString(PreferenceConstants.WEBSERVICE_HOST));
    }

    private Composite createTwoColumnContainer(final Composite parent) {
        final Composite container = new Composite(parent, SWT.FILL);
        container.setLayout(new GridLayout(2, true));
        return container;
    }

    private Composite createTableWrapper(final Composite container) {
        final Composite tableWrapper = new Composite(container, SWT.FILL);
        tableWrapper.setLayout(new ScaleOneDimensionLayout(SWT.HORIZONTAL));
        setGridData(tableWrapper, MIN_WIDTH_TABLE);
        return tableWrapper;
    }

    private void createTable(final Composite container) {
        final Composite tableComposite = new Composite(container, SWT.FILL);
        final TableViewer tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        final Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
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

    protected Image loadImage(final String name) {
        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, name);
        return desc.createImage();
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
        setGridData(detailsSection, MIN_WIDTH_DETAILS_SECTION);
        detailsSection.setLayout(new GridLayout(1, true));

        dependencyDetailsSection = new DependencyDetailsSection(this, detailsSection, dependencyStore, jobFactory);
        modelDetailsSection = new ModelDetailsSection(this, detailsSection, dependencyStore, archiveStore, jobFactory);
        commandSection = new CommandSection(detailsSection, dependencyStore, jobFactory);
    }

    private void setGridData(final Control control, final int minimumWidth) {
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.minimumWidth = minimumWidth;
        control.setLayoutData(gridData);
    }

    private void selectFile(final File file) {
        dependencyDetailsSection.selectFile(file);
        modelDetailsSection.selectFile(file);
    }

    @Override
    public boolean performOk() {
        saveWebserviceBaseurl();
        return super.performOk();
    }

    private void saveWebserviceBaseurl() {
        final String text = webserviceBaseurl.getText();
        getPreferenceStore().setValue(PreferenceConstants.WEBSERVICE_HOST, text);
    }

}
