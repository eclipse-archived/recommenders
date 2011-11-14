/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.extdoc.rcp.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.extdoc.rcp.ExtDocPlugin;
import org.eclipse.recommenders.extdoc.rcp.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public final class ExtDocPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ExtDocPreferencePage() {
        super(GRID);
        setPreferenceStore(ExtDocPlugin.preferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.USERNAME, "Username for Comments:", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.WEBSERVICE_HOST, "Webservice URL:", getFieldEditorParent()));

        final Composite parent = getFieldEditorParent();
        addSeparator(parent);
        addHoverNotice(parent);
    }

    private void addSeparator(final Composite parent) {
        new Label(parent, SWT.NONE);
        new Label(parent, SWT.NONE);
    }

    private void addHoverNotice(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("ExtDoc Hover");

        final GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.horizontalSpan = 2;
        group.setLayoutData(layoutData);

        final FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.marginWidth = 7;
        layout.marginHeight = 7;
        layout.spacing = 10;
        group.setLayout(layout);

        SwtFactory.createLabel(group, "To change hover settings, go to:", false);
        SwtFactory.createLabel(group, "Preferences > Java > Editor > Hovers.", false);
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

}
