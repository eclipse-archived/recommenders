/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy- initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import static org.eclipse.recommenders.internal.constructors.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.recommenders.internal.constructors.rcp.l10n.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class ConstructorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setDescription(Messages.PREFPAGE_DESCRIPTION_CONSTRUCTORS);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
    }

    @Override
    protected void createFieldEditors() {
        addField(new IntegerFieldEditor(PREF_MAX_NUMBER_OF_PROPOSALS, Messages.FIELD_LABEL_MAX_NUMBER_OF_PROPOSALS,
                getFieldEditorParent(), 3));
        addField(new IntegerFieldEditor(PREF_MIN_PROPOSAL_PROBABILITY, Messages.FIELD_LABEL_MIN_PROPOSAL_PROBABILITY,
                getFieldEditorParent(), 3));
        addField(new BooleanFieldEditor(PREF_DECORATE_PROPOSAL_ICON, Messages.FIELD_LABEL_DECORATE_PROPOSAL_ICON,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PREF_UPDATE_PROPOSAL_RELEVANCE, Messages.FIELD_LABEL_UPDATE_PROPOSAL_RELEVANCE,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PREF_DECORATE_PROPOSAL_TEXT, Messages.FIELD_LABEL_DECORATE_PROPOSAL_TEXT,
                getFieldEditorParent()));
    }
}
