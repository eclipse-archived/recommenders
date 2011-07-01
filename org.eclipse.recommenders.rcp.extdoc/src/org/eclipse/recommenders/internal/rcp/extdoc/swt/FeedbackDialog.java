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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import org.eclipse.recommenders.rcp.extdoc.AbstractDialog;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

final class FeedbackDialog extends AbstractDialog {

    private Text text;

    protected FeedbackDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void contentsCreated() {
        setOkButtonText("Submit");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        setTitle("Provide Feedback");
        setMessage("Please fill out the form.");
        setTitleImage("edit.png");

        final Composite composite = (Composite) super.createDialogArea(parent);
        final Composite area = SwtFactory.createGridComposite(composite, 1, 0, 10, 15, 20);
        new Label(area, SWT.NONE).setText("Your Feedback:");
        text = SwtFactory.createText(area, "", 350, 500);
        SwtFactory.createSeparator(composite);
        return composite;
    }

    @Override
    protected void okPressed() {
        // TODO: ...
        close();
    }

}
