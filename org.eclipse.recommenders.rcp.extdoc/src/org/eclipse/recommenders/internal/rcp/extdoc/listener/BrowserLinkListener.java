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
package org.eclipse.recommenders.internal.rcp.extdoc.listener;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.rcp.extdoc.listener.IBrowserListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

public final class BrowserLinkListener implements LocationListener {

    private final Map<Integer, IBrowserListener> listeners = new HashMap<Integer, IBrowserListener>();

    @Override
    public void changing(final LocationEvent event) {
        if (event.location.startsWith("about:blank#")) {
            final Integer hash = Integer.valueOf(event.location.substring(12));
            final IBrowserListener listener = listeners.get(hash);
            listener.activated();
        }
    }

    @Override
    public void changed(final LocationEvent event) {
    }

    public int addListener(final IBrowserListener listener) {
        listeners.put(listener.hashCode(), listener);
        return listener.hashCode();
    }

}
