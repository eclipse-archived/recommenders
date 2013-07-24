/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.overrides.rcp;

import static org.eclipse.recommenders.overrides.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    @Override
    protected void createFieldEditors() {
        addField(new IntegerFieldEditor(P_MAX_NUMBER_OF_PROPOSALS, "Maximal number of proposals:",
                getFieldEditorParent(), 3));
        addField(new IntegerFieldEditor(P_MIN_PROPOSAL_PROBABILITY, "Minimal probability threshold:",
                getFieldEditorParent(), 3));
        addField(new BooleanFieldEditor(P_DECORATE_PROPOSAL_ICON, "Enable proposal icon decorations.",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_UPDATE_PROPOSAL_RELEVANCE, "Enable proposal relevance updates.",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_DECORATE_PROPOSAL_TEXT, "Enable proposal text decorations.",
                getFieldEditorParent()));

    }

    @Override
    public void init(IWorkbench workbench) {
        setDescription("Settings for the Call Recommender Session Processor.\n");
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_NAME));
    }
}
