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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.swt;

import org.eclipse.recommenders.rcp.extdoc.AbstractDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public final class TemplateEditDialog extends AbstractDialog {

    public TemplateEditDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void contentsCreated() {
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        setTitle("Bla Bla");
        setMessage("Bla Bla");
        setTitleImage("edit.png");

        final Composite composite = (Composite) super.createDialogArea(parent);
        // TODO: ...
        return composite;
    }
}
