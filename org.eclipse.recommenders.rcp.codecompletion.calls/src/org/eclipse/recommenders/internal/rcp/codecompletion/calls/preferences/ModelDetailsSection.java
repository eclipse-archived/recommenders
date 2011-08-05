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
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.SearchManifestJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ModelDetailsSection extends AbstractDependencySection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionsText;
    private Text timestampText;
    private Button reresolveButton;
    private File file;
    private final RemoteResolverJobFactory jobFactory;

    public ModelDetailsSection(final PreferencePage preferencePage, final Composite parent,
            final ClasspathDependencyStore dependencyStore, final RemoteResolverJobFactory jobFactory) {
        super(preferencePage, parent, "Matched model");
        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;
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
        reresolveButton = createButton(parent, loadImage("/icons/obj16/refresh.gif"), createSelectionListener());
        reresolveButton.setToolTipText("Reresolve model");
    }

    public void selectFile(final File file) {
        this.file = file;
        if (file == null || !dependencyStore.containsManifest(file)) {
            resetTexts();
            setButtonsEnabled(false);
        } else {
            final Manifest manifest = dependencyStore.getManifest(file);
            nameText.setText(manifest.getName());
            versionsText.setText(manifest.getVersionRange().toString());
            timestampText.setText(DateFormat.getInstance().format(manifest.getTimestamp()));
            setButtonsEnabled(true);
        }
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (file != null) {
                    if (e.getSource() == reresolveButton) {
                        reresolveModel();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    private void reresolveModel() {
        dependencyStore.invalidateManifest(file);
        final SearchManifestJob job = jobFactory.create(file);
        job.schedule();
        reresolveButton.setEnabled(false);
    }
}
