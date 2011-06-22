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
import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

final class ProvidersTable {

    private static Table table;
    private static TableItem dragSourceItem;

    private static final Color COLOR_BLACK = SwtFactory.createColor(SWT.COLOR_BLACK);
    private static final Color COLOR_GRAY = SwtFactory.createColor(SWT.COLOR_DARK_GRAY);

    private final IEclipsePreferences preferences;
    private String preferencePrefix = "";

    private final CLabel locationLabel;
    private IJavaElementSelection lastSelection;

    ProvidersTable(final Composite parent, final int style) {
        final Composite composite = SwtFactory.createGridComposite(parent, 1, 0, 6, 0, 0);

        locationLabel = new CLabel(composite, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gridData.heightHint = ExtDocView.HEAD_LABEL_HEIGHT;
        locationLabel.setLayoutData(gridData);
        locationLabel.setImage(ExtDocPlugin.getIcon("eview16/context.gif"));
        locationLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

        table = new Table(composite, style);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        composite.setBackground(table.getBackground());

        table.addListener(SWT.Selection, new SelectionListener(this));
        enableDragAndDrop();

        preferences = ExtDocPlugin.getPreferences();
    }

    void addProvider(final Control providerControl, final String text, final Image image, final boolean checked) {
        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(text);
        tableItem.setData(providerControl);
        tableItem.setImage(image);
        tableItem.setChecked(false);
        setContentVisible(tableItem, false);
    }

    public TableItem[] getItems() {
        return table.getItems();
    }

    public void setContext(final IJavaElementSelection selection) {
        final JavaElementLocation location = selection.getElementLocation();
        if (lastSelection == null || lastSelection.getElementLocation() != location) {
            preferencePrefix = location == null ? "" : location.name();
            for (final TableItem item : table.getItems()) {
                final IProvider provider = (IProvider) ((Control) item.getData()).getData();
                boolean selectProvider = false;
                if (preferences.getBoolean(preferencePrefix + provider.getProviderName(), true)) {
                    selectProvider = provider.isAvailableForLocation(location);
                }
                item.setChecked(selectProvider);
                if (!selectProvider) {
                    setContentVisible(item, selectProvider);
                }
            }
        }
        lastSelection = selection;
        locationLabel.setText(location == null ? "" : location.getDisplayName());
    }

    void setContentVisible(final TableItem tableItem, final boolean visible) {
        final Control control = (Control) tableItem.getData();
        ((GridData) control.getLayoutData()).exclude = !visible;
        control.setVisible(visible);
        control.getParent().layout(true);

        tableItem.setGrayed(!visible);
        tableItem.setForeground(visible ? COLOR_BLACK : COLOR_GRAY);
    }

    private static void enableDragAndDrop() {
        final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(table, operations);
        source.setTransfer(types);
        source.addDragListener(new DragListener());

        final DropTarget target = new DropTarget(table, operations);
        target.setTransfer(types);
        target.addDropListener(new DropAdapter());
    }

    private static final class SelectionListener implements Listener {

        private final ProvidersTable table;

        SelectionListener(final ProvidersTable table) {
            this.table = table;
        }

        @Override
        public void handleEvent(final Event event) {
            final TableItem tableItem = (TableItem) event.item;
            final Control control = (Control) tableItem.getData();
            if (event.detail == SWT.CHECK) {
                table.preferences.putBoolean(
                        table.preferencePrefix + ((IProvider) control.getData()).getProviderName(),
                        tableItem.getChecked());
                if (tableItem.getGrayed()) {
                    if (tableItem.getChecked()) {
                        new ProviderUpdateJob(table, tableItem, table.lastSelection).schedule();
                    }
                } else {
                    table.setContentVisible(tableItem, tableItem.getChecked());
                }
            } else if (!tableItem.getGrayed()) {
                ((ScrolledComposite) control.getParent().getParent()).setOrigin(control.getLocation());
            }
        }

    }

    private static final class DragListener implements DragSourceListener {

        @Override
        public void dragStart(final DragSourceEvent event) {
            event.doit = true;
            dragSourceItem = table.getSelection()[0];
        };

        @Override
        public void dragSetData(final DragSourceEvent event) {
            event.data = dragSourceItem.getText();
        }

        @Override
        public void dragFinished(final DragSourceEvent event) {
            if (event.detail == DND.DROP_MOVE) {
                dragSourceItem.dispose();
            }
            dragSourceItem = null;
        }
    }

    private static final class DropAdapter extends DropTargetAdapter {

        @Override
        public void dragOver(final DropTargetEvent event) {
            event.feedback = DND.FEEDBACK_SCROLL;
            if (event.item == null) {
                event.feedback |= DND.FEEDBACK_INSERT_AFTER;
            } else {
                final Rectangle bounds = ((TableItem) event.item).getBounds();
                final Point pt = table.getShell().getDisplay().map(null, table, event.x, event.y);
                event.feedback |= pt.y < bounds.y + bounds.height >> 1 ? DND.FEEDBACK_INSERT_BEFORE
                        : DND.FEEDBACK_INSERT_AFTER;
            }
        }

        @Override
        public void drop(final DropTargetEvent event) {
            final TableItem item = (TableItem) event.item;
            int index;
            if (item == null) {
                index = table.getItemCount();
            } else {
                final Point pt = table.getShell().getDisplay().map(null, table, event.x, event.y);
                final Rectangle bounds = item.getBounds();
                final TableItem[] items = table.getItems();
                index = pt.y < bounds.y + bounds.height >> 1 ? 0 : 1;
                for (int i = 0; i < items.length; ++i) {
                    if (items[i].equals(item)) {
                        index += i;
                        break;
                    }
                }
            }

            final Control newItemControl = (Control) dragSourceItem.getData();
            if (index >= table.getItemCount()) {
                newItemControl.moveBelow((Control) table.getItem(index - 1).getData());
            } else {
                newItemControl.moveAbove((Control) table.getItem(index).getData());
            }
            newItemControl.getParent().layout();

            final TableItem newItem = new TableItem(table, SWT.NONE, index);
            newItem.setText(dragSourceItem.getText());
            newItem.setData(newItemControl);
            newItem.setImage(dragSourceItem.getImage());
            newItem.setChecked(dragSourceItem.getChecked());
            newItem.setGrayed(dragSourceItem.getGrayed());
        }
    }
}
