/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.HELP_URL;

import org.eclipse.jface.action.Action;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class EditorUtils {

    public static void addHelpActionToForm(ScrolledForm form) {
        SharedImages sharedImages = InjectionService.getInstance().getInjector().getInstance(SharedImages.class);
        Action showHelpAction = new Action(Messages.EDITOR_TOOLBAR_ITEM_HELP, sharedImages.getDescriptor(SharedImages.Images.ELCL_HELP)) {
            public void run() {
                BrowserUtils.openInExternalBrowser(HELP_URL);
            };
        };
        showHelpAction.setToolTipText(Messages.EDITOR_TOOLBAR_ITEM_HELP);
        form.getToolBarManager().add(showHelpAction);
        form.updateToolBar();
    }

}
