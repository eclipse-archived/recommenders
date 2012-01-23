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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.commons.udc.DependencyInformation;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionFinished;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.Events.DependencyResolutionRequested;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.classpath.DependencyInfoStore;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.parser.VersionParserFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

import com.google.common.base.Optional;

public class DependencyInfoSection extends AbstractSection {

    private final CallModelStore modelStore;
    private File file;
    private Text nameText;
    private Text versionText;
    private Text fingerprintText;
    private Button openDirectoryButton;
    private Button reresolveButton;
    private Button saveButton;

    public DependencyInfoSection(final PreferencePage preferencePage, final Composite parent,
            final CallModelStore modelStore) {
        super(preferencePage, parent, "Dependency details");
        this.modelStore = modelStore;

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
    protected void createButtons(final Composite parent) {
        reresolveButton = createButton(parent, loadImage("/icons/obj16/refresh.gif"), createSelectionListener());
        reresolveButton.setToolTipText("Automatically extract details");

        saveButton = createButton(parent, loadSharedImage(ISharedImages.IMG_ETOOL_SAVE_EDIT), createSelectionListener());
        saveButton.setToolTipText("Save details");

        openDirectoryButton = createButton(parent, loadImage("/icons/obj16/goto_folder.gif"), createSelectionListener());
        openDirectoryButton.setToolTipText("Open directory");
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
        final Optional<DependencyInformation> opt = modelStore.getDependencyInfoStore().getDependencyInfo(file);
        if (!opt.isPresent()) {
            resetTexts();
            setButtonsEnabled(false);
        } else {
            final DependencyInformation dependencyInfo = opt.get();
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
        // XXX this is a bit dirty...
        final Path path = new Path(file.getAbsolutePath());
        final IFile r = new org.eclipse.core.internal.resources.File(path, null) {

            @Override
            public IPath getLocation() {
                return path;
            }
        };

        modelStore.getDependencyInfoComputerService().onEvent(
                new DependencyResolutionRequested(new org.eclipse.jdt.internal.core.PackageFragmentRoot(r, null) {
                }));
        reresolveButton.setEnabled(false);
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
        final DependencyInfoStore depStore = modelStore.getDependencyInfoStore();
        final Optional<DependencyInformation> opt = depStore.getDependencyInfo(file);
        if (opt.isPresent()) {
            final String name = nameText.getText().trim();
            final String versionString = versionText.getText().trim();
            try {
                final Version version = parseVersion(versionString);
                final DependencyInformation oldInfo = opt.get();
                final DependencyInformation newInfo = new DependencyInformation();
                newInfo.symbolicName = name;
                newInfo.version = version;
                newInfo.jarFileFingerprint = oldInfo.jarFileFingerprint;
                newInfo.jarFileModificationDate = oldInfo.jarFileModificationDate;
                depStore.onEvent(new DependencyResolutionFinished(newInfo, file));
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

    // private final class PackageFragmentRootMock implements IPackageFragmentRoot {
    // @Override
    // public void save(final IProgressMonitor progress, final boolean force) throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void open(final IProgressMonitor progress) throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void makeConsistent(final IProgressMonitor progress) throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public boolean isOpen() {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public boolean isConsistent() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public boolean hasUnsavedChanges() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public IBuffer getBuffer() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String findRecommendedLineSeparator() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public void close() throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public Object getAdapter(final Class adapter) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public boolean isStructureKnown() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public boolean isReadOnly() {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public IResource getUnderlyingResource() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public ISchedulingRule getSchedulingRule() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    //
    //
    // @Override
    // public IJavaElement getPrimaryElement() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IPath getPath() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IJavaElement getParent() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IOpenable getOpenable() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IJavaProject getJavaProject() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IJavaModel getJavaModel() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String getHandleIdentifier() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public int getElementType() {
    // // TODO Auto-generated method stub
    // return 0;
    // }
    //
    // @Override
    // public String getElementName() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IResource getCorrespondingResource() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String getAttachedJavadoc(final IProgressMonitor monitor) throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IJavaElement getAncestor(final int ancestorType) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public boolean exists() {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public boolean hasChildren() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public IJavaElement[] getChildren() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public void move(final IPath destination, final int updateResourceFlags, final int updateModelFlags,
    // final IClasspathEntry sibling, final IProgressMonitor monitor) throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public boolean isExternal() {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public boolean isArchive() {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public IPath getSourceAttachmentRootPath() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IPath getSourceAttachmentPath() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IClasspathEntry getResolvedClasspathEntry() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IClasspathEntry getRawClasspathEntry() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public IPackageFragment getPackageFragment(final String packageName) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public Object[] getNonJavaResources() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public int getKind() throws JavaModelException {
    // // TODO Auto-generated method stub
    // return 0;
    // }
    //
    // @Override
    // public void delete(final int updateResourceFlags, final int updateModelFlags, final IProgressMonitor monitor)
    // throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public IPackageFragment createPackageFragment(final String name, final boolean force,
    // final IProgressMonitor monitor) throws JavaModelException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public void copy(final IPath destination, final int updateResourceFlags, final int updateModelFlags,
    // final IClasspathEntry sibling, final IProgressMonitor monitor) throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void attachSource(final IPath sourcePath, final IPath rootPath, final IProgressMonitor monitor)
    // throws JavaModelException {
    // // TODO Auto-generated method stub
    //
    // }
    // }
}
