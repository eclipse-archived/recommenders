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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.internal.rcp.extdoc.AbstractSelectableBrowserElement;

public final class CommentsIcon extends AbstractSelectableBrowserElement {

    private final URL imageUrl;

    public CommentsIcon(final Dialog dialog) {
        super(dialog);
        imageUrl = getImageUrl("comments.png");
    }

    @Override
    public String getHtml(final String href) {
        return "<a href=\"" + href + "\"><img src=\"" + imageUrl + "\" /></a>";
    }

}
