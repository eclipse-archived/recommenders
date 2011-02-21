/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public PreferencePage() {
        super(GRID);
        setPreferenceStore(CodesearchPlugin.getDefault().getPreferenceStore());
        setDescription("Code Examples Recommender Preferences:");
    }

    @Override
    public void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceInitializer.SERVER_URL, "Server URL", getFieldEditorParent()));
    }

    @Override
    public void init(final IWorkbench workbench) {
    }
}
