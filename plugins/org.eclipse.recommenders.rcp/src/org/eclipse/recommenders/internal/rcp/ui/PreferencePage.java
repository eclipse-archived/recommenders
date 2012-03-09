/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.ui;

import static org.eclipse.recommenders.rcp.RecommendersPlugin.P_REPOSITORY_ENABLE_AUTO_DOWNLOAD;
import static org.eclipse.recommenders.rcp.RecommendersPlugin.P_REPOSITORY_URL;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public PreferencePage() {
        super(GRID);
        setPreferenceStore(RecommendersPlugin.getDefault().getPreferenceStore());
        setDescription("Model Repository Preferences. All models used by Code Recommenders will be pulled from this repository.");
    }

    @Override
    public void createFieldEditors() {
        addField(new StringFieldEditor(P_REPOSITORY_URL, "Model Repository:", getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD, "Enable auto-download.",
                getFieldEditorParent()));
    }

    @Override
    public void init(final IWorkbench workbench) {
    }
}