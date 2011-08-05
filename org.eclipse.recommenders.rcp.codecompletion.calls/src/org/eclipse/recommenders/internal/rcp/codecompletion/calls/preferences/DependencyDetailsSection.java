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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.commons.udc.ClasspathDependencyInformation;
import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.parser.VersionParserFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.ClasspathDependencyStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.RemoteResolverJobFactory;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.SearchManifestJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

public class DependencyDetailsSection extends AbstractDependencySection {

    private final ClasspathDependencyStore dependencyStore;
    private Text nameText;
    private Text versionText;
    private Text fingerprintText;
    private Button openDirectoryButton;
    private File file;
    private Button reresolveButton;
    private final RemoteResolverJobFactory jobFactory;
    private Button saveButton;

    public DependencyDetailsSection(final PreferencePage preferencePage, final Composite parent,
            final ClasspathDependencyStore dependencyStore, final RemoteResolverJobFactory jobFactory) {
        super(preferencePage, parent, "Dependency details");

        this.dependencyStore = dependencyStore;
        this.jobFactory = jobFactory;
    }

    @Override
    protected void createDetailsContainer(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.NONE | SWT.BORDER);

        createLabel(parent, "Version:");
        versionText = createText(parent, SWT.NONE | SWT.BORDER);

        createLabel(parent, "Fingerprint:");
        fingerprintText = createText(parent, SWT.READ_ONLY);
    }

    @Override
    protected void createButtons(final Composite container) {
        reresolveButton = createButton(container, loadImage("/icons/obj16/refresh.gif"), createSelectionListener());
        reresolveButton.setToolTipText("Automatically extract details");

        openDirectoryButton = createButton(container, loadSharedImage(ISharedImages.IMG_OBJ_FOLDER),
                createSelectionListener());
        openDirectoryButton.setToolTipText("Open directory");

        saveButton = createButton(container, loadSharedImage(ISharedImages.IMG_ETOOL_SAVE_EDIT),
                createSelectionListener());
        saveButton.setToolTipText("Save details");
    }

    private SelectionListener createSelectionListener() {
        return new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (file != null) {
                    if (e.getSource() == openDirectoryButton) {
                        openDirectory();
                    } else if (e.getSource() == reresolveButton) {
                        reresolveDependency();
                    } else if (e.getSource() == saveButton) {
                        save();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    public void selectFile(final File file) {
        this.file = file;
        if (file == null || !dependencyStore.containsClasspathDependencyInfo(file)) {
            resetTexts();
            setButtonsEnabled(false);
        } else {
            final ClasspathDependencyInformation dependencyInfo = dependencyStore.getClasspathDependencyInfo(file);
            nameText.setText(dependencyInfo.symbolicName);
            versionText.setText(getVersionText(dependencyInfo.version));
            fingerprintText.setText(dependencyInfo.jarFileFingerprint);
            setButtonsEnabled(true);
        }
    }

    private String getVersionText(final Version version) {
        if (version.isUnknown()) {
            return "";
        } else {
            return version.toString();
        }
    }

    private void openDirectory() {
        final File openFile = file.getParentFile();
        Program.launch(openFile.getAbsolutePath());
    }

    private void reresolveDependency() {
        dependencyStore.invalidateClasspathDependencyInfo(file);
        scheduleResolvingJob();
        reresolveButton.setEnabled(false);
    }

    private void scheduleResolvingJob() {
        final SearchManifestJob job = jobFactory.create(file);
        job.schedule();
    }

    @Override
    protected void validate(final PreferencePage preferencePage) {
        final String versionString = versionText.getText().trim();
        try {
            parseVersion(versionString);
            preferencePage.setErrorMessage(null);
            saveButton.setEnabled(true);
        } catch (final RuntimeException e) {
            preferencePage.setErrorMessage(String.format("Cannot parse '%s' as version.", versionString));
            saveButton.setEnabled(false);
        }
    }

    private void save() {
        final ClasspathDependencyInformation dependencyInfo = dependencyStore.getClasspathDependencyInfo(file);
        if (dependencyInfo != null) {
            final String name = nameText.getText().trim();
            final String versionString = versionText.getText().trim();
            try {
                final Version version = parseVersion(versionString);
                dependencyInfo.symbolicName = name;
                dependencyInfo.version = version;
                dependencyStore.putClasspathDependencyInfo(file, dependencyInfo);
                dependencyStore.invalidateManifest(file);
                scheduleResolvingJob();
                selectFile(file);
            } catch (final RuntimeException e) {
            }
        }
    }

    private Version parseVersion(final String versionString) {
        if (versionString.length() != 0) {
            final Version version = VersionParserFactory.getCompatibleParser(versionString).parse(versionString);
            if (version != null) {
                return version;
            }
        }
        return Version.UNKNOWN;
    }
}
