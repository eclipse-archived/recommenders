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
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.SearchManifestJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DependencyDetailsSection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionText;
    private Text fingerprintText;
    private Button openDirectoryButton;
    private File file;
    private Button reresolveButton;
    private final RemoteResolverJobFactory jobFactory;

    public DependencyDetailsSection(final Composite parent, final ClasspathDependencyStore dependencyStore,
            final RemoteResolverJobFactory jobFactory) {
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;

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
        nameText = createText(parent, SWT.NONE | SWT.BORDER);

        createLabel(parent, "Version:");
        versionText = createText(parent, SWT.NONE | SWT.BORDER);

        createLabel(parent, "Fingerprint:");
        fingerprintText = createText(parent, SWT.READ_ONLY);
    }

    private void createButtons(final Composite group) {
        final Composite container = new Composite(group, SWT.NONE);
        container.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.BEGINNING)
                .create());
        container.setLayout(new RowLayout());

        reresolveButton = createButton(container, "Automatically extract details",
                loadImage("/icons/obj16/refresh.gif"));
        openDirectoryButton = createButton(container, "Open directory", PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJ_FOLDER));
    }

    private Image loadImage(final String name) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.recommenders.rcp.codecompletion.calls", name)
                .createImage();
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (file != null) {
                    if (e.getSource() == openDirectoryButton) {
                        final File openFile = file.getParentFile();
                        Program.launch(openFile.getAbsolutePath());
                    } else if (e.getSource() == reresolveButton) {
                        dependencyStore.invalidateClasspathDependencyInfo(file);
                        final SearchManifestJob job = jobFactory.create(file);
                        job.schedule();
                        reresolveButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    private Button createButton(final Composite container, final String toolTip, final Image image) {
        final Button button = new Button(container, SWT.PUSH);
        button.setEnabled(false);
        button.setImage(image);
        button.setToolTipText(toolTip);
        button.addSelectionListener(createSelectionListener());
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
            reresolveButton.setEnabled(false);
        } else {
            final ClasspathDependencyInformation dependencyInfo = dependencyStore.getClasspathDependencyInfo(file);
            nameText.setText(dependencyInfo.symbolicName);
            versionText.setText(getVersionText(dependencyInfo.version));
            fingerprintText.setText(dependencyInfo.jarFileFingerprint);
            openDirectoryButton.setEnabled(true);
            reresolveButton.setEnabled(true);
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
