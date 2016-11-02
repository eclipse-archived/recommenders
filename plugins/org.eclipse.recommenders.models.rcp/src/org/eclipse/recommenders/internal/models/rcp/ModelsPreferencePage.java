/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Gottschaemmer, Olav Lenz - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.internal.models.rcp.Constants.*;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class ModelsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final ModelsRcpPreferences preferences;
    private final SharedImages images;

    @Inject
    public ModelsPreferencePage(ModelsRcpPreferences preferences, SharedImages images) {
        super(GRID);
        this.preferences = preferences;
        this.images = images;
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
        setMessage(Messages.PREFPAGE_TITLE_MODEL_REPOSITORIES);
        setDescription(Messages.PREFPAGE_DESCRIPTION_MODEL_REPOSITORIES);
    }

    @Override
    protected void createFieldEditors() {
        ModelRepositoriesFieldEditor repoEditor = new ModelRepositoriesFieldEditor(PREF_REPOSITORY_URL_LIST,
                getFieldEditorParent(), preferences, images);
        addField(repoEditor);
        addField(new BooleanFieldEditor(PREF_REPOSITORY_ENABLE_AUTO_DOWNLOAD, Messages.FIELD_LABEL_ENABLE_AUTO_DOWNLOAD,
                getFieldEditorParent()));
    }
}
