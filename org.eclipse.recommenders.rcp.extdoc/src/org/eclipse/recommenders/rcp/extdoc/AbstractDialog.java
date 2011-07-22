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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.osgi.framework.Bundle;

public abstract class AbstractDialog extends TitleAreaDialog {

    private static final Bundle BUNDLE = ExtDocPlugin.getDefault().getBundle();

    protected AbstractDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected final Control createContents(final Composite parent) {
        final Control control = super.createContents(parent);
        Dialog.applyDialogFont(control);
        contentsCreated();
        return control;
    }

    protected final void setTitleImage(final String imageUri) {
        final String entry = String.format("icons/full/wizban/%s", imageUri);
        final Image image = ImageDescriptor.createFromURL(BUNDLE.getEntry(entry)).createImage();
        setTitleImage(image);
    }

    protected void contentsCreated() {
    }

    protected final void setOkButtonText(final String text) {
        getButton(IDialogConstants.OK_ID).setText(text);
    }

}
