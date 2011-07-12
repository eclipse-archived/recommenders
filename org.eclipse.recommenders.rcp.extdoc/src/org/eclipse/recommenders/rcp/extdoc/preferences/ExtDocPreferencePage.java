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
package org.eclipse.recommenders.rcp.extdoc.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public final class ExtDocPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ExtDocPreferencePage() {
        super(GRID);
        setPreferenceStore(ExtDocPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.USERNAME, "Username for Comments:", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.WEBSERVICE_HOST, "Webservice URL:", getFieldEditorParent()));
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

}
