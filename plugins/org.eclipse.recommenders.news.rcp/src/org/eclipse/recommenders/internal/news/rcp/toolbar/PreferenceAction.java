/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class PreferenceAction extends Action {

    @Override
    public void run() {
        PreferencesUtil.createPreferenceDialogOn(null, "org.eclipse.recommenders.news.rcp.preferencePage", null, null) //$NON-NLS-1$
                .open();
    }

    @Override
    public String getText() {
        return Messages.LABEL_PREFERENCES;
    }
}
