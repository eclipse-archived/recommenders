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
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.recommenders.rcp.utils.ScaleOneDimensionLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;

public class PackageSelectionComposite extends Composite {
    private static final String PACKAGE_TABLE_TOOLTIP = "Enter an expression to select packages.\nE.g. \"*\" for all packages or \"org.eclipse.* \" for packages starting with \"org.eclipse.\"";
    private Table includedPackagesTable;
    private Button moveToIncludesButton;
    private Table excludedPackagesTable;
    private Tree previewTree;
    private Button moveToExcludesButton;

    public PackageSelectionComposite(final Composite parent, final int style) {
        super(parent, style);
        final GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);
        // Composite expressionComposite = new Composite(this, SWT.None);
        // expressionComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL,
        // true, true, 1, 1));
        // expressionComposite.setLayout(new GridLayout(3, false));

        createHeaderSection(this);

        createIncludeTable(this);

        createMoveExpressionButtons(this);

        createExcludeTable(this);
        createPreviewSection(this);
    }

    private void createMoveExpressionButtons(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.None);
        composite.setLayout(new GridLayout(1, false));

        moveToIncludesButton = new Button(composite, SWT.NONE);
        moveToIncludesButton.setToolTipText("Move the selected exclude to includes");
        moveToIncludesButton.setText("<<");

        moveToExcludesButton = new Button(composite, SWT.NONE);
        moveToExcludesButton.setToolTipText("Move the selected include to excludes");
        moveToExcludesButton.setText(">>");
    }

    private void createPreviewSection(final Composite parent) {
        final Label preview = new Label(parent, SWT.None);
        preview.setText("Preview:");
        new Label(parent, SWT.NONE);
        new Label(parent, SWT.NONE);

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new ScaleOneDimensionLayout(SWT.HORIZONTAL));
        final GridData gd_previewTree = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        composite.setLayoutData(gd_previewTree);

        previewTree = new Tree(composite, SWT.BORDER);
    }

    private void createExcludeTable(final Composite parent) {
        excludedPackagesTable = createTable(parent);
        excludedPackagesTable.setToolTipText(PACKAGE_TABLE_TOOLTIP);

    }

    private Table createTable(final Composite parent) {
        final Composite scaledComposite = createScaledOneDimensionComposite(parent);
        final Composite tableComposite = new Composite(scaledComposite, SWT.NONE);

        final Table table = new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(false);
        table.setLinesVisible(true);

        setSingleColumnTableLayout(tableComposite, table);
        return table;
    }

    private void setSingleColumnTableLayout(final Composite tableComposite, final Table table) {
        final TableColumnLayout layout = new TableColumnLayout();
        tableComposite.setLayout(layout);
        final TableColumn singleColumn = new TableColumn(table, SWT.NONE);
        singleColumn.setResizable(false);
        layout.setColumnData(singleColumn, new ColumnWeightData(100));
    }

    private Composite createScaledOneDimensionComposite(final Composite parent) {
        final Composite result = new Composite(parent, SWT.NONE);
        result.setLayout(new ScaleOneDimensionLayout(SWT.HORIZONTAL));
        result.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        return result;
    }

    private void createIncludeTable(final Composite parent) {
        includedPackagesTable = createTable(parent);
        includedPackagesTable.setToolTipText(PACKAGE_TABLE_TOOLTIP);

    }

    private void createHeaderSection(final Composite parent) {
        final CLabel lblIncludedPackages = new CLabel(parent, SWT.NONE);
        lblIncludedPackages.setImage(ImageProvider.getInstance().getPackageMatchesExpressionsImage());
        lblIncludedPackages.setText("Included Packages:");

        new Label(parent, SWT.None);

        final CLabel lblExcludedPackages = new CLabel(parent, SWT.NONE);
        lblExcludedPackages.setImage(ImageProvider.getInstance().getPackageDoesNotMatchExpressionsImage());
        lblExcludedPackages.setText("Excluded Packages:");
    }

    public Table getIncludedPackagesTable() {
        return includedPackagesTable;
    }

    public Button getMoveToIncludesButton() {
        return moveToIncludesButton;
    }

    public Button getMoveToExcludesButton() {
        return moveToExcludesButton;
    }

    public Table getExcludedPackagesTable() {
        return excludedPackagesTable;
    }

    public Tree getPreviewTree() {
        return previewTree;
    }

}
