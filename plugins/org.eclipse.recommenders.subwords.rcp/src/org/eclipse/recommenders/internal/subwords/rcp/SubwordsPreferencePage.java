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

import static org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_INFORMATION;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
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
        setDescription(Messages.PREFPAGE_DESCRIPTION_SUBWORDS_PREFIX_LENGTH);
    }

    @Override
    protected void createFieldEditors() {
        IntegerFieldEditor prefixLengthEditor = new IntegerFieldEditor(Constants.PREF_MIN_PREFIX_LENGTH_FOR_TYPES,
                Messages.PREFPAGE_LABEL_PREFIX_LENGTH, getFieldEditorParent());
        prefixLengthEditor.setValidRange(1, 99);
        Text control = prefixLengthEditor.getTextControl(getFieldEditorParent());
        ControlDecoration dec = new ControlDecoration(control, SWT.TOP | SWT.LEFT, getFieldEditorParent());
        FieldDecoration infoDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(DEC_INFORMATION);
        dec.setImage(infoDecoration.getImage());
        dec.setDescriptionText(Messages.PREFPAGE_TOOLTIP_PREFIX_LENGTH);
        addField(prefixLengthEditor);
    }
}
