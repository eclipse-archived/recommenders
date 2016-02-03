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
package org.eclipse.recommenders.internal.calls.rcp;

import static org.eclipse.recommenders.internal.calls.rcp.Constants.PREF_HIGHLIGHT_USED_PROPOSALS;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.recommenders.completion.rcp.AbstractCompletionPreferencePage;
import org.eclipse.recommenders.internal.calls.rcp.l10n.Messages;

public class CallsPreferencePage extends AbstractCompletionPreferencePage {

    public CallsPreferencePage() {
        super(Constants.BUNDLE_NAME, Messages.PREFPAGE_DESCRIPTION_CALLS);
    }

    @Override
    protected void createFieldEditors() {
        super.createFieldEditors();

        addField(new BooleanFieldEditor(PREF_HIGHLIGHT_USED_PROPOSALS, Messages.FIELD_LABEL_HIGHLIGHT_USED_PROPOSALS,
                getFieldEditorParent()));

        Dialog.applyDialogFont(getControl());
    }
}
