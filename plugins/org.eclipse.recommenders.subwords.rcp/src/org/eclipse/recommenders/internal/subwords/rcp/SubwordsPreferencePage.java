/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class SubwordsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public SubwordsPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
        setMessage(Messages.PREFPAGE_TITLE_SUBWORDS);
        setDescription(Messages.PREFPAGE_DESCRIPTION_SUBWORDS);
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(Constants.PREF_RESTRICT_INITIAL_CONSTRUCTOR_PROPOSALS,
                Messages.FIELD_LABEL_RESTRICT_INITIAL_CONSTRUCTOR_PROPOSALS, getFieldEditorParent()));
        addField(new BooleanFieldEditor(Constants.PREF_RESTRICT_INITIAL_TYPE_PROPOSALS,
                Messages.FIELD_LABEL_RESTRICT_INITIAL_TYPE_PROPOSALS, getFieldEditorParent()));
    }
}
