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
package org.eclipse.recommenders.internal.udc.ui;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class TreeComposite extends Composite {
    public static final String FILTER_TEXT_DEFAULT = "type filter text";
    private static final int MIN_WIDTH_TABLE = 50;
    private final Tree tree;
    private Button btnSelectAll;
    private Button btnDeselectAll;
    private Text filterText;

    public TreeComposite(final Composite parent, final int style) {
        super(parent, style);
        final GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        createFilterSection();

        final Composite tableWrapper = createTableWrapper(this);
        final Composite treeComposite = createTreeSection(tableWrapper);

        tree = new Tree(treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);

        createButtonsSection();
        tree.setFocus();

    }

    private void setGridData(final Control control, final int minimumWidth) {
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.minimumWidth = minimumWidth;
        gridData.widthHint = 60;
        gridData.heightHint = 60;
        control.setLayoutData(gridData);
    }

    private Composite createTableWrapper(final Composite container) {
        final Composite tableWrapper = new Composite(container, SWT.FILL);
        tableWrapper.setLayout(new FillLayout());
        setGridData(tableWrapper, MIN_WIDTH_TABLE);
        return tableWrapper;
    }

    private Composite createTreeSection(final Composite parent) {
        final Composite treeComposite = new Composite(parent, SWT.FILL);
        treeComposite.setLayout(new FillLayout());
        return treeComposite;
    }

    private void createFilterSection() {
        filterText = new Text(this, SWT.BORDER);
        filterText.setText(FILTER_TEXT_DEFAULT);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);
    }

    private void createButtonsSection() {
        final Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        btnSelectAll = new Button(composite, SWT.NONE);
        btnSelectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnSelectAll.setText("Select All");

        btnDeselectAll = new Button(composite, SWT.NONE);
        btnDeselectAll.setBounds(0, 0, 75, 25);
        btnDeselectAll.setText("Deselect All");
    }

    public Tree getTree() {
        return tree;
    }

    public Button getSelectAllButton() {
        return btnSelectAll;
    }

    public Button getDeselectAllButton() {
        return btnDeselectAll;
    }

    public Text getFilterText() {
        return filterText;
    }
}
