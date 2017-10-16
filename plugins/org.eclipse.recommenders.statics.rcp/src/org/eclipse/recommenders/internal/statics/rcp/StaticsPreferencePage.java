/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.statics.rcp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.completion.rcp.AbstractCompletionPreferencePage;
import org.eclipse.recommenders.internal.statics.rcp.l10n.Messages;

public class StaticsPreferencePage extends AbstractCompletionPreferencePage {

    public StaticsPreferencePage() {
        super(Constants.BUNDLE_NAME, Messages.PREFPAGE_DESCRIPTION_STATICS);
    }

    @Override
    protected void createFieldEditors() {
        super.createFieldEditors();
        Dialog.applyDialogFont(getControl());
    }
}
