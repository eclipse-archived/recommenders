/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.menus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;

public class NoNewsMenuListener implements IMenuListener {

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        Action action = new Action() {

            @Override
            public void run() {
            }
        };
        action.setText(Messages.LABEL_NO_NEW_MESSAGES);
        manager.add(action);
    }

}
