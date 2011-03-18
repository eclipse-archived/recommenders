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
package org.eclipse.recommenders.internal.rcp.codesearch.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CodesearchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public CodesearchPreferencePage() {
        super(GRID);
        setPreferenceStore(CodesearchPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.WEBSERVICE_HOST, "Webservice URL:", getFieldEditorParent()));
    }

    @Override
    public void init(final IWorkbench workbench) {

    }

}
