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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class DependencyDetailsSection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionText;
    private Text fingerprintText;
    private Button openDirectoryButton;
    private Button openFileButton;
    private SelectionListener selectionListener;
    private File file;

    public DependencyDetailsSection(final Composite parent, final ClasspathDependencyStore dependencyStore) {
        this.dependencyStore = dependencyStore;

        final Composite group = createGroup(parent);
        createDetails(group);
        createButtons(group);
    }

    private Composite createGroup(final Composite parent) {
        final Group section = new Group(parent, SWT.NONE);
        section.setText("Dependency details");
        section.setLayout(new GridLayout(2, false));
        section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        return section;
    }

    private void createDetails(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.NONE);

        createLabel(parent, "Version:");
        versionText = createText(parent, SWT.NONE);

        createLabel(parent, "Fingerprint:");
        fingerprintText = createText(parent, SWT.READ_ONLY);
    }

    private void createButtons(final Composite group) {
        final Composite container = new Composite(group, SWT.NONE);
        container.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.BEGINNING)
                .create());
        container.setLayout(new RowLayout());

        createSelectionListener();
        openDirectoryButton = createButton(container, "Open directory", ISharedImages.IMG_OBJ_FOLDER);
        openFileButton = createButton(container, "Open file", ISharedImages.IMG_OBJ_FILE);
    }

    private void createSelectionListener() {
        selectionListener = new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (file != null) {
                    File openFile;
                    if (e.getSource() == openDirectoryButton) {
                        openFile = file.getParentFile();
                    } else {
                        openFile = file;
                    }
                    Program.launch(openFile.getAbsolutePath());
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    private Button createButton(final Composite container, final String toolTip, final String imageName) {
        final Button button = new Button(container, SWT.PUSH);
        button.setEnabled(false);
        button.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(imageName));
        button.setToolTipText(toolTip);
        button.addSelectionListener(selectionListener);
        return button;
    }

    private Label createLabel(final Composite parent, final String text) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    private Text createText(final Composite parent, final int style) {
        final Text text = new Text(parent, style);
        text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(150, SWT.DEFAULT)
                .align(GridData.FILL, GridData.BEGINNING).create());
        return text;
    }

    public void selectFile(final File file) {
        this.file = file;
        if (file == null || !dependencyStore.containsClasspathDependencyInfo(file)) {
            nameText.setText("");
            versionText.setText("");
            fingerprintText.setText("");
            openDirectoryButton.setEnabled(false);
            openFileButton.setEnabled(false);
        } else {
            final ClasspathDependencyInformation dependencyInfo = dependencyStore.getClasspathDependencyInfo(file);
            nameText.setText(dependencyInfo.symbolicName);
            versionText.setText(getVersionText(dependencyInfo.version));
            fingerprintText.setText(dependencyInfo.jarFileFingerprint);
            openDirectoryButton.setEnabled(true);
            openFileButton.setEnabled(true);
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
