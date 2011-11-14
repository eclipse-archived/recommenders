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
package org.eclipse.recommenders.internal.udc.ui.projectselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.recommenders.internal.udc.ui.TreeComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ProjectSelectionComposite extends Composite {
    private final TreeComposite treeComposite;
    private final Button onlyNewButton;

    public TreeComposite getTreeComposite() {
        return treeComposite;
    }

    public Button getOnlyNewButton() {
        return onlyNewButton;
    }

    public ProjectSelectionComposite(final Composite parent, final int style) {
        super(parent, style);
        final GridLayout gridLayout_1 = new GridLayout(1, false);
        setLayout(gridLayout_1);

        onlyNewButton = new Button(this, SWT.CHECK);
        onlyNewButton.setText("Show only new projects");

        treeComposite = new TreeComposite(this, SWT.NONE);
        final GridLayout gridLayout = (GridLayout) treeComposite.getLayout();
        gridLayout.marginWidth = 0;
        treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    }
}
