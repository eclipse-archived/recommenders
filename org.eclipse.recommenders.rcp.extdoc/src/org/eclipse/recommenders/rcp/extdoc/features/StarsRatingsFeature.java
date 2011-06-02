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
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.server.extdoc.IRatingsServer;

public final class StarsRatingsFeature {

    private final IJavaElement element;
    private final IRatingsServer server;
    private final IProvider provider;

    public StarsRatingsFeature(final IJavaElement element, final IRatingsServer server, final IProvider provider) {
        this.element = element;
        this.server = server;
        this.provider = provider;
    }

    public int getAverageRating() {
        return server.getAverageRating(element);
    }

    public int getUserRating() {
        return server.getUserRating(element);
    }

    public void addRating(final int stars) {
        server.addRating(element, stars);
        provider.redraw();
    }

}
