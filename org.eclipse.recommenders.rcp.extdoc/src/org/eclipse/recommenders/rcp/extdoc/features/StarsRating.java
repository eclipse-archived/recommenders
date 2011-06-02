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

import java.net.URL;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.internal.rcp.extdoc.AbstractSelectableBrowserElement;
import org.eclipse.recommenders.rcp.extdoc.IProvider;

public final class StarsRating extends AbstractSelectableBrowserElement {

    private final URL star;
    private final URL starEmpty;
    private final URL starActive;

    private final IJavaElement element;
    private final IStarsRatingsServer server;
    private final IProvider provider;

    public StarsRating(final IJavaElement element, final IStarsRatingsServer server, final IProvider provider) {
        this.element = element;
        this.server = server;
        this.provider = provider;
        star = getImageUrl("star.png");
        starEmpty = getImageUrl("star_empty.png");
        starActive = getImageUrl("star_active.png");
    }

    @Override
    public String getHtml(final String href) {
        final int averageRating = server.getAverageRating(element);
        final int userRating = server.getUserRating(element);

        final StringBuilder html = new StringBuilder(64);
        for (int i = 1; i <= 5; ++i) {
            if (userRating < 1) {
                html.append("<a href=\"" + href + i + "\">");
            }
            html.append("<img src=\"" + (averageRating < i ? starEmpty : star) + "\" />");
            if (userRating < 1) {
                html.append("</a>");
            }
        }
        return html.toString();
    }

    @Override
    public void selected(final String linkAppendix) {
        final int stars = Integer.parseInt(linkAppendix);
        server.addRating(element, stars);
        provider.redraw();
    }

}
