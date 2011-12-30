/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.Date;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.internal.udc.ui.wizard.uploadrequest.Request4UploadWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class OpenWizardDelegate implements IStartup {

    private static final long askInterval = 3 * 24 * 60 * 60 * 1000;

    @Override
    public void earlyStartup() {
        if (!PreferenceUtil.needToOpenUploadWizard()) {
            return;
        }
        if (!lastTimeAskedIsLongEnoughAgo()) {
            return;
        }
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                final WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(),
                        new Request4UploadWizard());
                dialog.open();
            }
        });

    }

    private boolean lastTimeAskedIsLongEnoughAgo() {
        final long lastTimeAsked = PreferenceUtil.getLastTimeOpenedUploadWizard();
        if (lastTimeAsked == 0) {
            UploadPreferences.setLastTimeOpenedUploadWizard(System.currentTimeMillis());
            return false;
        }

        final Date currentDate = new Date();

        final Date nextDateToAsk = new Date(lastTimeAsked + askInterval);
        return currentDate.after(nextDateToAsk);
    }

}
