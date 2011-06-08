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

import com.google.common.base.Preconditions;

import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.AbstractDialog;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public final class DeleteDialog extends AbstractDialog {

    private final IDeletionProvider provider;
    private final String objectName;
    private final Object object;

    protected DeleteDialog(final IDeletionProvider provider, final Object object, final String objectName) {
        super(provider.getShell());
        this.provider = provider;
        this.object = object;
        this.objectName = objectName;
    }

    @Override
    protected void contentsCreated() {
        // TODO Auto-generated method stub
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Image image = AbstractUIPlugin.imageDescriptorFromPlugin(
                ExtDocPlugin.getDefault().getBundle().getSymbolicName(), "icons/full/wizban/delete.png").createImage();

        setTitle(String.format("Delete %s", objectName));
        setMessage("Are you sure?");
        setTitleImage(image);

        final Composite composite = (Composite) super.createDialogArea(parent);
        final Composite area = SwtFactory.createGridComposite(composite, 1, 0, 10, 15, 20);

        SwtFactory.createLabel(area, "Are you sure to delete " + objectName + "?", true);
        SwtFactory.createLabel(area, "", false);

        SwtFactory.createCheck(area, "Do not display this item anymore.", true);
        SwtFactory.createCheck(area, "Send anonymous information about this deletion to provider as feedback.", false);
        SwtFactory.createSeparator(composite);
        return composite;
    }

    @Override
    protected void okPressed() {
        try {
            provider.requestDeletion(object);
            provider.redraw();
        } finally {
            Preconditions.checkArgument(close());
        }
    }

}
