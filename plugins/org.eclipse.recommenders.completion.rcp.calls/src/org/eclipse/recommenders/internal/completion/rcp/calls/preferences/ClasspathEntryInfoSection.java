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

import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.READ_ONLY;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.assistedinject.Assisted;

public class ClasspathEntryInfoSection extends AbstractSection {

    private Text nameText;
    private Text versionText;
    private Text fingerprintText;
    private Button openDirectoryButton;
    private DataBindingContext ctx;

    private IClasspathEntryInfoProvider cpeInfoProvider;
    private ClasspathEntryInfo cpeInfo;
    private File file;

    @Inject
    public ClasspathEntryInfoSection(@Assisted final PreferencePage preferencePage, @Assisted final Composite parent,
            final IClasspathEntryInfoProvider cpeInfoProvider) {
        // XXX: Johannes, do we need the preference page here?
        super(preferencePage, parent, "Dependency details");
        this.cpeInfoProvider = cpeInfoProvider;
    }

    @Override
    protected void createDetailsContainer(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, READ_ONLY | BORDER);

        createLabel(parent, "Version:");
        versionText = createText(parent, READ_ONLY | BORDER);

        createLabel(parent, "Fingerprint:");
        fingerprintText = createText(parent, READ_ONLY | BORDER);
    }

    @Override
    protected void createButtons(final Composite parent) {
        openDirectoryButton = createButton(parent, loadImage("/icons/obj16/goto_folder.gif"));
        openDirectoryButton.setToolTipText("Open directory");
        openDirectoryButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (file != null) {
                    final File openFile = file.getParentFile();
                    Program.launch(openFile.getAbsolutePath());
                }
            }
        });
    }

    public void selectionChanged(final File file) {
        this.file = file;
        cpeInfo = cpeInfoProvider.getInfo(file).or(ClasspathEntryInfo.NULL);
        bindValues();
    }

    private void bindValues() {
        if (ctx != null) {
            ctx.dispose();
        }
        ctx = new DataBindingContext();
        bind("symbolicName", nameText);
        bind("version", versionText);
        bind("fingerprint", fingerprintText);
        setButtonsEnabled(true);
    }

    private void bind(String property, Text widget) {
        IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(widget);
        IObservableValue modelValue = BeanProperties.value(ClasspathEntryInfo.class, property).observe(cpeInfo);
        ctx.bindValue(widgetValue, modelValue);
    }

}
