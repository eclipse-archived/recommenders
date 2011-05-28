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
package org.eclipse.recommenders.rcp.extdoc.swt;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractDialog extends TitleAreaDialog {

    public AbstractDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected final Control createContents(final Composite parent) {
        final Control control = super.createContents(parent);
        contentsCreated();
        return control;
    }

    protected abstract void contentsCreated();

    protected final void setOkButtonText(final String text) {
        getButton(IDialogConstants.OK_ID).setText(text);
    }

}
