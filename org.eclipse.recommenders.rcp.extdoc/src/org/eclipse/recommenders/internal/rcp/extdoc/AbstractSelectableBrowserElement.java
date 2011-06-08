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

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.Dialog;

public abstract class AbstractSelectableBrowserElement implements ISelectableBrowserElement {

    private Dialog dialog;
    private URL imageUrl;

    protected AbstractSelectableBrowserElement() {
    }

    protected AbstractSelectableBrowserElement(final Dialog dialog, final String image) {
        this.dialog = Preconditions.checkNotNull(dialog);
        imageUrl = getImageUrl(image);
    }

    protected final URL getImageUrl(final String filename) {
        try {
            return FileLocator.toFileURL(ExtDocPlugin.getBundleEntry("/icons/full/eview16/" + filename));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getHtml(final String href) {
        return "<a href=\"" + href + "\"><img src=\"" + imageUrl + "\" /></a>";
    }

    @Override
    public void selected(final String linkAppendix) {
        dialog.open();
    }

}
