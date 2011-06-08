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
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public final class CommentsDialog extends AbstractDialog {

    private final IProvider provider;
    private final ICommentsServer server;
    private final Object object;
    private final String objectName;

    public CommentsDialog(final Shell parentShell, final ICommentsServer server, final IProvider provider,
            final Object object, final String objectName) {
        super(parentShell);
        setBlockOnOpen(false);

        this.server = server;
        this.provider = provider;
        this.object = object;
        this.objectName = objectName;
    }

    @Override
    protected void contentsCreated() {
        setOkButtonText("Save");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Image image = AbstractUIPlugin.imageDescriptorFromPlugin(
                ExtDocPlugin.getDefault().getBundle().getSymbolicName(), "icons/full/wizban/comments.png")
                .createImage();

        setTitle(String.format("Comments for %s", objectName));
        setMessage("Bla Bla");
        setTitleImage(image);

        final Composite composite = (Composite) super.createDialogArea(parent);
        final Composite area = SwtFactory.createGridComposite(composite, 1, 0, 10, 15, 20);
        new Label(area, SWT.NONE).setText("Under construction");
        SwtFactory.createSeparator(composite);
        return composite;
    }

    @Override
    protected void okPressed() {
        try {
            // TODO: ...
            provider.redraw();
        } finally {
            Preconditions.checkArgument(close());
        }
    }

}
