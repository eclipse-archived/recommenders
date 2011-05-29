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
package org.eclipse.recommenders.rcp.extdoc.browser;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;

public final class EditListener implements IBrowserElementListener {

    private final URL imageUrl;
    private final Dialog dialog;

    public EditListener(final Dialog dialog) {
        this.dialog = dialog;
        try {
            imageUrl = FileLocator.toFileURL(ExtDocPlugin.getBundleEntry("/icons/full/eview16/edit.png"));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getHtml(final String href) {
        return "<a href=\"" + href + "\"><img src=\"" + imageUrl + "\" /></a>";
    }

    @Override
    public void selected() {
        dialog.open();
    }

}
