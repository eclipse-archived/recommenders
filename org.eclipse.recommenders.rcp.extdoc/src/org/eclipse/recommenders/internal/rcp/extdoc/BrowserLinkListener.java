/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

public final class BrowserLinkListener implements LocationListener {

    private final Map<Integer, ISelectableBrowserElement> listeners = new HashMap<Integer, ISelectableBrowserElement>();

    @Override
    public void changing(final LocationEvent event) {
        if (event.location.startsWith("about:blank#")) {
            final String link = event.location.substring(12);
            final int length = link.indexOf('?');
            final Integer hash = Integer.valueOf(link.substring(0, length));
            final ISelectableBrowserElement listener = listeners.get(hash);
            listener.selected(link.substring(length + 1));
        }
    }

    @Override
    public void changed(final LocationEvent event) {
        // Not of interest to us.
    }

    public int addListener(final ISelectableBrowserElement listener) {
        listeners.put(listener.hashCode(), listener);
        return listener.hashCode();
    }

}
