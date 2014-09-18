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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class EditorUtils {

    public static void addActionToForm(ScrolledForm form, Action action, String tooltip) {
        action.setToolTipText(tooltip);
        form.getToolBarManager().add(action);
        form.updateToolBar();
    }
}
