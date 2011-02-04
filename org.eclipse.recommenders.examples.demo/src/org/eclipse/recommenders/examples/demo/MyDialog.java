/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.examples.demo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

/**
 * Demo outline:
 * <ol>
 * <li>intelligent calls code completion</li>
 * <li>dynamic code templates</li>
 * <li>extended javadoc</li>
 * <li>call-chain completion</li>
 * <ol>
 */
public class MyDialog extends Dialog {

    private Text swtTextWidget;

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite container = createContainer(parent);

        swtTextWidget = new Text(container, SWT.BORDER);

        final Button b = new Button(parent, 0);
        b.addSelectionListener(null);
        b.setSelection(true);
        b.setText(null);
        final IWorkbenchHelpSystem help = PlatformUI.getWorkbench().getHelpSystem();

        return container;
    }

    private Composite createContainer(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(null);
        return container;
    }

    protected MyDialog(final IShellProvider parentShell) {
        super(parentShell);
    }
}
