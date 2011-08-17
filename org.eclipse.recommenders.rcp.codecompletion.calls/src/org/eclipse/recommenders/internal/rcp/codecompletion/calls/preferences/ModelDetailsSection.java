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
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ManifestResolvementInformation;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchive;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.SearchManifestJob;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

public class ModelDetailsSection extends AbstractDependencySection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionsText;
    private Text timestampText;
    private Button reresolveButton;
    private File file;
    private final RemoteResolverJobFactory jobFactory;
    private Button selectModelButton;
    private final PreferencePage preferencePage;
    private final ModelArchiveStore archiveStore;
    private Text resolvedTimestampText;
    private Text resolvingStrategyText;

    public ModelDetailsSection(final PreferencePage preferencePage, final Composite parent,
            final ClasspathDependencyStore dependencyStore, final ModelArchiveStore archiveStore,
            final RemoteResolverJobFactory jobFactory) {
        super(preferencePage, parent, "Matched model");
        this.preferencePage = preferencePage;
        this.dependencyStore = dependencyStore;
        this.archiveStore = archiveStore;
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

        createLabel(parent, "Resolved at:");
        resolvedTimestampText = createText(parent, SWT.READ_ONLY);

        createLabel(parent, "Resolving strategy:");
        resolvingStrategyText = createText(parent, SWT.READ_ONLY);
        resolvingStrategyText
                .setToolTipText("Use reresolve model button for automatic resolvement or select a model file on your own.");
    }

    @Override
    protected void createButtons(final Composite parent) {
        reresolveButton = createButton(parent, loadImage("/icons/obj16/refresh.gif"), createSelectionListener());
        reresolveButton.setToolTipText("Reresolve model");

        selectModelButton = createButton(parent, loadSharedImage(ISharedImages.IMG_OBJ_FOLDER),
                createSelectionListener());
        selectModelButton.setToolTipText("Select model file");
    }

    public void selectFile(final File file) {
        this.file = file;
        setButtonsEnabled(file != null);
        if (file == null || !dependencyStore.containsManifest(file)) {
            resetTexts();
        } else {
            final ManifestResolvementInformation resolvementInfo = dependencyStore.getManifestResolvementInfo(file);
            final Manifest manifest = resolvementInfo.getManifest();
            nameText.setText(manifest.getName());
            versionsText.setText(manifest.getVersionRange().toString());
            timestampText.setText(formatDate(manifest.getTimestamp()));
            resolvedTimestampText.setText(formatDate(resolvementInfo.getResolvingTimestamp()));
            resolvingStrategyText.setText(resolvementInfo.isResolvedManual() ? "manual" : "automatic");
        }
    }

    private String formatDate(final Date date) {
        return DateFormat.getInstance().format(date);
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (file != null) {
                    if (e.getSource() == reresolveButton) {
                        reresolveModel();
                    } else if (e.getSource() == selectModelButton) {
                        selectModelFile();
                        selectFile(file);
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

    private void selectModelFile() {
        final FileDialog dialog = new FileDialog(preferencePage.getShell(), SWT.SINGLE);
        dialog.setFilterExtensions(new String[] { "*.zip" });
        dialog.setFilterNames(new String[] { "Model files" });
        final String selection = dialog.open();
        if (selection != null) {
            final File file = new File(selection);
            if (file.exists()) {
                registerModel(file);
            }
        }
    }

    private void registerModel(final File modelFile) {
        try {
            final File temp = File.createTempFile("model.", ".zip");
            FileUtils.copyFile(modelFile, temp);
            final ModelArchive modelArchive = new ModelArchive(temp);
            final Manifest manifest = modelArchive.getManifest();
            archiveStore.register(modelArchive);
            dependencyStore.putManifest(file, manifest, true);
        } catch (final Exception e) {
            preferencePage.setErrorMessage("Selected file could not be used as model.");
            RecommendersPlugin.logError(e, "Selected file could not be used as model.");
        }
    }
}
