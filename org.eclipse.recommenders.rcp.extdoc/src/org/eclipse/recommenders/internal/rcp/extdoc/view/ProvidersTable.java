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
package org.eclipse.recommenders.internal.rcp.extdoc.view;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

final class ProvidersTable {

    private static final Color COLOR_BLACK = SwtFactory.createColor(SWT.COLOR_BLACK);
    private static final Color COLOR_GRAY = SwtFactory.createColor(SWT.COLOR_DARK_GRAY);

    private CLabel locationLabel;
    private Table table;
    private final IEclipsePreferences preferences;
    private IJavaElementSelection lastSelection;

    ProvidersTable(final Composite parent, final ProviderStore providerStore) {
        final Composite composite = SwtFactory.createGridComposite(parent, 1, 0, 6, 0, 0);
        createLocationLabel(composite);
        createTable(composite, providerStore);
        composite.setBackground(table.getBackground());
        preferences = ExtDocPlugin.getPreferences();
    }

    private void createLocationLabel(final Composite composite) {
        locationLabel = new CLabel(composite, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gridData.heightHint = ExtDocView.HEAD_LABEL_HEIGHT;
        locationLabel.setLayoutData(gridData);
        locationLabel.setImage(ExtDocPlugin.getIcon("eview16/context.gif"));
        locationLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        locationLabel.setToolTipText("Provider selection is sensitive to the displayed code location.");
    }

    private void createTable(final Composite composite, final ProviderStore providerStore) {
        table = new Table(composite, SWT.CHECK | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addListener(SWT.Selection, new ProvidersTableSelectionListener(this));
        enableDragAndDrop(providerStore);
    }

    TableItem addProvider(final Composite provider, final String text, final Image image) {
        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(text);
        tableItem.setData(provider);
        tableItem.setChecked(false);
        tableItem.setImage(image);
        tableItem.setData("image", image);
        setContentVisible(tableItem, false, true);
        return tableItem;
    }

    public TableItem[] getItems() {
        return table.getItems();
    }

    public void setContext(final IJavaElementSelection selection) {
        final JavaElementLocation location = selection.getElementLocation();
        if (lastSelection == null || lastSelection.getElementLocation() != location) {
            for (final TableItem item : getItems()) {
                updateProvider(item, location);
            }
        }
        lastSelection = selection;
        locationLabel.setText(location == null ? "" : location.getDisplayName());
    }

    private void updateProvider(final TableItem item, final JavaElementLocation location) {
        final IProvider provider = (IProvider) ((Control) item.getData()).getData();
        boolean selectProvider = false;
        if (preferences.getBoolean(getPreferenceId(provider, location), true)) {
            selectProvider = provider.isAvailableForLocation(location);
        }
        item.setChecked(selectProvider);
        if (!selectProvider) {
            setContentVisible(item, false, true);
        }
    }

    static void setContentVisible(final TableItem tableItem, final boolean visible, final boolean updateTableItem) {
        final Composite control = (Composite) tableItem.getData();
        ((GridData) control.getLayoutData()).exclude = !visible;
        control.setVisible(visible);
        control.getParent().layout(true, false);

        if (updateTableItem) {
            tableItem.setGrayed(!visible);
            tableItem.setForeground(visible ? COLOR_BLACK : COLOR_GRAY);
        }
    }

    static String getPreferenceId(final IProvider provider, final JavaElementLocation location) {
        return "provider" + provider.hashCode() + location;
    }

    private void enableDragAndDrop(final ProviderStore providerStore) {
        final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
        final ProvidersTableDragListener dragListener = createDragSource(types, operations);
        createDropTarget(types, operations, dragListener, providerStore);
    }

    private ProvidersTableDragListener createDragSource(final Transfer[] types, final int operations) {
        final DragSource source = new DragSource(table, operations);
        source.setTransfer(types);
        final ProvidersTableDragListener dragListener = new ProvidersTableDragListener(table);
        source.addDragListener(dragListener);
        return dragListener;
    }

    private void createDropTarget(final Transfer[] types, final int operations,
            final ProvidersTableDragListener dragListener, final ProviderStore providerStore) {
        final DropTarget target = new DropTarget(table, operations);
        target.setTransfer(types);
        target.addDropListener(new ProvidersTableDropAdapter(table, dragListener, providerStore));
    }

    IJavaElementSelection getLastSelection() {
        return lastSelection;
    }

    void setChecked(final String preferenceId, final boolean isChecked) {
        preferences.putBoolean(preferenceId, isChecked);
    }

    @Provisional
    public Table getTable() {
        return table;
    }
}
