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
package org.eclipse.recommenders.internal.extdoc.rcp.preferences;

import javax.inject.Inject;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public final class ExtdocPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final ClientConfiguration client;

    @Inject
    public ExtdocPreferencePage(@Extdoc final ClientConfiguration client, @Extdoc final IPreferenceStore preferences) {
        super(GRID);
        this.client = client;
        setPreferenceStore(preferences);
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.WEBSERVICE_HOST, "Webservice URL:", getFieldEditorParent()));
    }

    @Override
    public boolean performOk() {
        final boolean result = super.performOk();
        updateClientBaseurl();
        return result;
    }

    private void updateClientBaseurl() {
        client.setBaseUrl(getPreferenceStore().getString(PreferenceConstants.WEBSERVICE_HOST));
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

}
