/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.wizard.export;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.io.File;

import org.eclipse.recommenders.internal.udc.CompilationUnitZipExporter;
import org.eclipse.recommenders.internal.udc.ExporterFactory;
import org.eclipse.recommenders.internal.udc.ICompilationUnitExporter;
import org.eclipse.recommenders.internal.udc.ui.wizard.WizardPageTemplate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class ExportDestinationPage extends WizardPageTemplate {
    public ExportDestinationPage() {
    }

    private Button btnToServer;
    private Button btnToZip;
    private Group grpTargetFile;
    private Text textTargetFile;
    private Button btnBrowse;
    private final SelectionListener listener = new SelectionAdapter() {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            setFileSelectionEnabled(btnToZip.getSelection());
            updateExporter();
        }

    };

    ICompilationUnitExporter exporter = ExporterFactory.createCompilationUnitServerExporter();

    @Override
    public void createControl(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        setControl(container);
        container.setLayout(new GridLayout(1, false));

        btnToServer = new Button(container, SWT.RADIO);
        btnToServer.setSelection(true);
        btnToServer.setText("Upload to Server");
        btnToServer.addSelectionListener(listener);

        btnToZip = new Button(container, SWT.RADIO);
        btnToZip.setText("Export as Zip File");
        btnToZip.addSelectionListener(listener);

        grpTargetFile = new Group(container, SWT.NONE);
        grpTargetFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpTargetFile.setText("Target File");
        grpTargetFile.setLayout(new GridLayout(2, false));

        textTargetFile = new Text(grpTargetFile, SWT.BORDER);
        textTargetFile.setEnabled(false);
        textTargetFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textTargetFile.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setDestinationFile(textTargetFile.getText());
            }
        });

        btnBrowse = new Button(grpTargetFile, SWT.NONE);
        btnBrowse.setEnabled(false);
        btnBrowse.setSize(75, 25);
        btnBrowse.setText("Browse..");
        btnBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                openFileSelectionDialog();
            }
        });

    }

    public ICompilationUnitExporter getExporter() {
        return exporter;
    }

    @Override
    public String getPageTitle() {
        return "Export Destination";
    }

    private void openFileSelectionDialog() {
        final FileDialog dlg = new FileDialog(this.getContainer().getShell(), SWT.SINGLE | SWT.SAVE);
        dlg.setFilterExtensions(new String[] { "*.zip" });
        final String destinationFile = dlg.open();
        textTargetFile.setText(destinationFile);
        setDestinationFile(destinationFile);
    }

    private void setDestinationFile(final String filePath) {
        try {
            exporter = new CompilationUnitZipExporter(new File(filePath));
            setErrorMessage(null);
        } catch (final Exception e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void setFileSelectionEnabled(final boolean selectionEnabled) {
        textTargetFile.setEnabled(selectionEnabled);
        btnBrowse.setEnabled(selectionEnabled);
    }

    private void updateExporter() {
        if (btnToZip.getSelection()) {
            setDestinationFile(textTargetFile.getText());
        } else {
            exporter = ExporterFactory.createCompilationUnitServerExporter();
        }
    }
}
