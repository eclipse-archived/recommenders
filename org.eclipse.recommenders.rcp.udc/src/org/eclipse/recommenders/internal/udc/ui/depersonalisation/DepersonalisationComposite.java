/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

import org.eclipse.recommenders.internal.udc.ui.CompareComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class DepersonalisationComposite extends Composite {
    private CompareComposite compareComposite;
    private Button depersonalizeButton;

    public DepersonalisationComposite(final Composite parent, final int style) {
        super(parent, style);
        setLayout(new GridLayout());

        createDepersonalisationSection();

        createPreviewSection();
    }

    private void createDepersonalisationSection() {
        depersonalizeButton = new Button(this, SWT.CHECK);
        final GridData gd_depersonalizeButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_depersonalizeButton.verticalIndent = 5;
        depersonalizeButton.setLayoutData(gd_depersonalizeButton);
        depersonalizeButton.setText("Depersonalize usage data");
    }

    private void createPreviewSection() {
        compareComposite = new CompareComposite(this, SWT.NONE);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalIndent = 5;
        compareComposite.setLayoutData(gd);
    }

    public CompareComposite getCompareComposite() {
        return compareComposite;
    }

    public Button getDepersonalizeButton() {
        return depersonalizeButton;
    }
}
