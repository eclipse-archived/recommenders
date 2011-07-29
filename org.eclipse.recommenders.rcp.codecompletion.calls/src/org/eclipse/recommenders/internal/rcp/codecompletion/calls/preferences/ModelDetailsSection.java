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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.commons.lfm.Manifest;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ModelDetailsSection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionsText;
    private Text timestampText;

    public ModelDetailsSection(final Composite parent, final ClasspathDependencyStore dependencyStore) {
        this.dependencyStore = dependencyStore;

        final Composite group = createGroup(parent);
        createDetails(group);
    }

    private Composite createGroup(final Composite parent) {
        final Group section = new Group(parent, SWT.NONE);
        section.setText("Matched model");
        section.setLayout(new GridLayout(2, false));
        section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        return section;
    }

    private void createDetails(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.READ_ONLY);

        createLabel(parent, "Versions:");
        versionsText = createText(parent, SWT.READ_ONLY);

        createLabel(parent, "Built at:");
        timestampText = createText(parent, SWT.READ_ONLY);
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
        if (file == null || !dependencyStore.containsManifest(file)) {
            nameText.setText("");
            versionsText.setText("");
            timestampText.setText("");
        } else {
            final Manifest manifest = dependencyStore.getManifest(file);
            nameText.setText(manifest.getName());
            versionsText.setText(manifest.getVersionRange().toString());
            timestampText.setText(DateFormat.getInstance().format(manifest.getTimestamp()));
        }

    }
}
