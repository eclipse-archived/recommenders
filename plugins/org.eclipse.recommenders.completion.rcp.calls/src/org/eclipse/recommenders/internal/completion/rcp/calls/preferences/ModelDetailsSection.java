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

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.commons.udc.DependencyInformation;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.ManifestResolutionRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.ManifestResolverInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Optional;

public class ModelDetailsSection extends AbstractSection {

    private final CallModelStore modelStore;
    private Text nameText;
    private Text versionsText;
    private Text timestampText;
    private Button reresolveButton;
    private File file;
    private Button selectModelButton;
    private Text resolvedTimestampText;
    private Text resolvingStrategyText;
    private Button deleteModelButton;
    private final DependencyInfoStore depStore;

    public ModelDetailsSection(final PreferencePage preferencePage, final Composite parent,
            final CallModelStore modelStore) {
        super(preferencePage, parent, "Matched model");
        this.modelStore = modelStore;
        this.depStore = modelStore.getDependencyInfoStore();
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

        // selectModelButton = createButton(parent, loadSharedImage(ISharedImages.IMG_OBJ_FOLDER),
        // createSelectionListener());
        // selectModelButton.setToolTipText("Select model file");
        //
        // deleteModelButton = createButton(parent, loadImage("/icons/obj16/trash.gif"), new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected(final SelectionEvent e) {
        // if (file == null) {
        // return;
        // }
        // Optional<ManifestResolverInfo> opt = dependencyStore.getManifestResolverInfo(file);
        // if (!opt.isPresent()) {
        // return;
        // }
        // final ManifestResolverInfo resolvementInfo = opt.get();
        // final Manifest manifest = resolvementInfo.getManifest();
        // dependencyStore.invalidateManifest(file);
        // archiveStore.removeModelArchive(manifest);
        // }
        // });
        // deleteModelButton.setToolTipText("Remove model from store.");
    }

    public void selectFile(final File file) {
        this.file = file;
        setButtonsEnabled(file != null);
        final Optional<ManifestResolverInfo> opt = depStore.getManifestResolverInfo(file);
        if (!opt.isPresent()) {
            resetTexts();
        } else {
            final ManifestResolverInfo resolvementInfo = opt.get();
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
        return new SelectionAdapter() {

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
        };
    }

    private void reresolveModel() {
        final Optional<DependencyInformation> opt = depStore.getDependencyInfo(file);
        if (opt.isPresent()) {
            modelStore.getManifestResolverService().onEvent(new ManifestResolutionRequested(opt.get()));
        }
        reresolveButton.setEnabled(false);
    }

    private void selectModelFile() {
        // final FileDialog dialog = new FileDialog(preferencePage.getShell(), SWT.SINGLE);
        // dialog.setFilterExtensions(new String[] { "*.zip" });
        // dialog.setFilterNames(new String[] { "Model files" });
        // final String selection = dialog.open();
        // if (selection != null) {
        // final File file = new File(selection);
        // if (file.exists()) {
        // registerModel(file);
        // }
        // }
    }

    // private void registerModel(final File modelFile) {
    // try {
    // final File temp = File.createTempFile("model.", ".zip");
    // FileUtils.copyFile(modelFile, temp);
    // final ModelArchive modelArchive = new ModelArchive(temp);
    // final Manifest manifest = modelArchive.getManifest();
    // archiveStore.register(modelArchive);
    // dependencyStore.putManifest(file, manifest, true);
    // } catch (final Exception e) {
    // preferencePage.setErrorMessage("Selected file could not be used as model.");
    // RecommendersPlugin.logError(e, "Selected file could not be used as model.");
    // }
    // }
}
