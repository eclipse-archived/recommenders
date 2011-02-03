/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.examples.demo;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * Two tools to show:
 * 
 * <ul>
 * <li>Example Code Search: Go into {@link #setMessage(String)} and trigger code
 * completion until you see the proposals for "searching code  examples"</li>
 * <li>Call Chain Completion: in {@link #setMessage(String)} Put your cursor
 * behind</li>
 * </ul>
 */
public class MyView extends ViewPart {

    public void setMessage(final String newMessage) {

        // How do I get an instance of IStatusLineManager?

        final IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
        // manager.setMessage(newMessage);

    }

    @Override
    public void createPartControl(final Composite parent) {
    }

    @Override
    public void setFocus() {
    }
}
