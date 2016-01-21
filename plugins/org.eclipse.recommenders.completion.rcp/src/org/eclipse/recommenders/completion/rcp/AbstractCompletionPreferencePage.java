/**
 * Copyright (c) 2016 Yasser Aziza.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasser Aziza - initial implementation
 */
package org.eclipse.recommenders.completion.rcp;

import static org.eclipse.recommenders.completion.rcp.PreferenceConstants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.recommenders.internal.completion.rcp.l10n.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public abstract class AbstractCompletionPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private final String bundleId;
    private final String description;

    public AbstractCompletionPreferencePage(String bundleId, String description) {
        this.bundleId = bundleId;
        this.description = description;
    }

    @Override
    public void init(IWorkbench workbench) {
        setDescription(description);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, bundleId));
    }

    @Override
    protected void createFieldEditors() {
        addField(new IntegerFieldEditor(PREF_MAX_NUMBER_OF_PROPOSALS, Messages.FIELD_LABEL_MAX_NUMBER_OF_PROPOSALS,
                getFieldEditorParent(), 3));
        addField(new IntegerFieldEditor(PREF_MIN_PROPOSAL_PERCENTAGE, Messages.FIELD_LABEL_MIN_PROPOSAL_PERCENTAGE,
                getFieldEditorParent(), 3));
        addField(new BooleanFieldEditor(PREF_DECORATE_PROPOSAL_ICON, Messages.FIELD_LABEL_DECORATE_PROPOSAL_ICON,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PREF_UPDATE_PROPOSAL_RELEVANCE, Messages.FIELD_LABEL_UPDATE_PROPOSAL_RELEVANCE,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PREF_DECORATE_PROPOSAL_TEXT, Messages.FIELD_LABEL_DECORATE_PROPOSAL_TEXT,
                getFieldEditorParent()));

        Dialog.applyDialogFont(getControl());

    }
}
