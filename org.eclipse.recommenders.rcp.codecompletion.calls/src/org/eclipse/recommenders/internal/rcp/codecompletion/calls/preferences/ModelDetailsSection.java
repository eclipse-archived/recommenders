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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ModelDetailsSection extends AbstractDependencySection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionsText;
    private Text timestampText;

    public ModelDetailsSection(final PreferencePage preferencePage, final Composite parent,
            final ClasspathDependencyStore dependencyStore) {
        super(preferencePage, parent, "Matched model");
        this.dependencyStore = dependencyStore;
    }

    @Override
    protected void createDetailsContainer(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.READ_ONLY);

        createLabel(parent, "Versions:");
        versionsText = createText(parent, SWT.READ_ONLY);

        createLabel(parent, "Built at:");
        timestampText = createText(parent, SWT.READ_ONLY);
    }

    @Override
    protected void createButtons(final Composite parent) {
        // TODO Auto-generated method stub

    }

    public void selectFile(final File file) {
        if (file == null || !dependencyStore.containsManifest(file)) {
            resetTexts();
        } else {
            final Manifest manifest = dependencyStore.getManifest(file);
            nameText.setText(manifest.getName());
            versionsText.setText(manifest.getVersionRange().toString());
            timestampText.setText(DateFormat.getInstance().format(manifest.getTimestamp()));
        }

    }
}
