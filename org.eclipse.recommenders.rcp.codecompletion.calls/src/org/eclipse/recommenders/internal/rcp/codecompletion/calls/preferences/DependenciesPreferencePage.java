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
import java.text.DateFormat;

import javax.inject.Inject;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.recommenders.commons.lfm.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class DependenciesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final int MIN_WIDTH_TABLE = 200;
    private static final int MIN_WIDTH_DETAILS_LABEL = 60;
    private static final int MIN_WIDTH_DETAILS_TEXT = 150;

    private final ClasspathDependencyStore dependencyStore;
    private Text symbolicNameText;
    private Text versionText;
    private Text fingerprintText;
    private Text manifestNameText;
    private Text manifestVersionText;
    private Text manifestTimestampText;
    private File currentFile;
    private Button openDirectoryButton;
    private Button openFileButton;

    @Inject
    public DependenciesPreferencePage(final ClasspathDependencyStore dependencyStore) {
        this.dependencyStore = dependencyStore;
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
        final TableViewer tableViewer = new TableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
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
                    show(null);
                } else {
                    show((File) ((StructuredSelection) selection).getFirstElement());
                }
            }
        });
    }

    private void createColumns(final TableViewer tableViewer, final TableColumnLayout tableColumnLayout) {
        final TableViewerColumn column = createTableViewerColumn(tableViewer, "File", 200, 0);
        column.getColumn().setResizable(false);

        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(final Object element) {
                return ((File) element).getName();
            }
        });
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100));
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
        setGridData(detailsSection, MIN_WIDTH_DETAILS_LABEL + MIN_WIDTH_DETAILS_TEXT);
        detailsSection.setLayout(new GridLayout(1, true));
        createDependencyInfoSection(detailsSection);
        createModelSection(detailsSection);
    }

    private void createDependencyInfoSection(final Composite container) {
        final Group section = new Group(container, SWT.NONE);
        section.setText("Dependency details");
        section.setLayout(new GridLayout(2, false));
        setGridData(section, MIN_WIDTH_DETAILS_LABEL + MIN_WIDTH_DETAILS_TEXT);

        final Label symbolicNameLabel = new Label(section, SWT.NONE);
        symbolicNameLabel.setText("Name:");
        symbolicNameText = new Text(section, SWT.NONE);
        setGridData(symbolicNameText, MIN_WIDTH_DETAILS_TEXT);

        final Label versionLabel = new Label(section, SWT.NONE);
        versionLabel.setText("Version:");
        versionText = new Text(section, SWT.NONE);
        setGridData(versionText, MIN_WIDTH_DETAILS_TEXT);

        final Label fingerprintLabel = new Label(section, SWT.NONE);
        fingerprintLabel.setText("Fingerprint:");
        fingerprintText = new Text(section, SWT.READ_ONLY);
        setGridData(fingerprintText, MIN_WIDTH_DETAILS_TEXT);

        openDirectoryButton = new Button(section, SWT.PUSH);
        openDirectoryButton.setEnabled(false);
        openDirectoryButton
                .setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
        openDirectoryButton.setToolTipText("Open directory");
        openDirectoryButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                openCurrentFileFolder();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });

        openFileButton = new Button(section, SWT.PUSH);
        openFileButton.setEnabled(false);
        openFileButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
        openFileButton.setToolTipText("Open file");
        openFileButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                openCurrentFile();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
    }

    protected void openCurrentFile() {
        if (currentFile != null) {
            Program.launch(currentFile.getAbsolutePath());
        }
    }

    protected void openCurrentFileFolder() {
        if (currentFile != null) {
            Program.launch(currentFile.getParentFile().getAbsolutePath());
        }
    }

    private void createModelSection(final Composite container) {
        final Group modelSection = new Group(container, SWT.NONE);
        modelSection.setText("Matched model");
        modelSection.setLayout(new GridLayout(2, false));
        setGridData(modelSection, MIN_WIDTH_DETAILS_LABEL + MIN_WIDTH_DETAILS_TEXT);

        final Label manifestNameLabel = new Label(modelSection, SWT.NONE);
        manifestNameLabel.setSize(MIN_WIDTH_DETAILS_LABEL, 0);
        manifestNameLabel.setText("Name:");
        manifestNameText = new Text(modelSection, SWT.READ_ONLY);
        setGridData(manifestNameText, MIN_WIDTH_DETAILS_TEXT);

        final Label versionLabel = new Label(modelSection, SWT.NONE);
        versionLabel.setText("Versions:");
        manifestVersionText = new Text(modelSection, SWT.READ_ONLY);
        setGridData(manifestVersionText, MIN_WIDTH_DETAILS_TEXT);

        final Label timestampLabel = new Label(modelSection, SWT.NONE);
        timestampLabel.setText("Built at:");
        manifestTimestampText = new Text(modelSection, SWT.READ_ONLY);
        setGridData(manifestTimestampText, MIN_WIDTH_DETAILS_TEXT);
    }

    private void setGridData(final Control control, final int minimumWidth) {
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.minimumWidth = minimumWidth;
        control.setLayoutData(gridData);
    }

    private void show(final File file) {
        this.currentFile = file;
        showDependencyInformation();
        showManifestInformation();
    }

    private void showDependencyInformation() {
        if (currentFile == null || !dependencyStore.containsClasspathDependencyInfo(currentFile)) {
            symbolicNameText.setText("");
            versionText.setText("");
            fingerprintText.setText("");
            openFileButton.setEnabled(false);
            openDirectoryButton.setEnabled(false);
        } else {
            final ClasspathDependencyInformation dependencyInfo = dependencyStore
                    .getClasspathDependencyInfo(currentFile);
            symbolicNameText.setText(dependencyInfo.symbolicName);
            versionText.setText(getVersionText(dependencyInfo.version));
            fingerprintText.setText(dependencyInfo.jarFileFingerprint);
            openFileButton.setEnabled(true);
            openDirectoryButton.setEnabled(true);
        }
    }

    private void showManifestInformation() {
        if (currentFile == null || !dependencyStore.containsManifest(currentFile)) {
            manifestNameText.setText("");
            manifestVersionText.setText("");
            manifestTimestampText.setText("");
        } else {
            final Manifest manifest = dependencyStore.getManifest(currentFile);
            manifestNameText.setText(manifest.getName());
            manifestVersionText.setText(manifest.getVersionRange().toString());
            manifestTimestampText.setText(DateFormat.getInstance().format(manifest.getTimestamp()));
        }
    }

    private String getVersionText(final Version version) {
        if (version.isUnknown()) {
            return "";
        } else {
            return version.toString();
        }
    }
}
