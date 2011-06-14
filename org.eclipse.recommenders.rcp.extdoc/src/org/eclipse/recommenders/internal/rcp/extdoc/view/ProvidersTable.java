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

import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
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

    private static Color blackColor;
    private static Color grayColor;

    protected ProvidersTable(final Composite parent, final int style) {
        table = new Table(parent, style);
        table.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final TableItem tableItem = (TableItem) event.item;
                if (!tableItem.getGrayed()) {
                    if (event.detail == SWT.CHECK) {
                        setChecked(tableItem, tableItem.getChecked());
                        ((Control) tableItem.getData()).getParent().layout(true);
                    } else {
                        final Control control = (Control) tableItem.getData();
                        control.setFocus();
                    }
                }
            }
        });
        enableDragAndDrop();

        blackColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        grayColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
    }

    private void enableDragAndDrop() {
        final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(table, operations);
        source.setTransfer(types);
        source.addDragListener(new DragListener());

        final DropTarget target = new DropTarget(table, operations);
        target.setTransfer(types);
        target.addDropListener(new DropAdapter());
    }

    protected void addProvider(final Control providerControl, final String text, final Image image,
            final boolean checked) {
        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(text);
        tableItem.setData(providerControl);
        tableItem.setImage(image);
        setGrayed(tableItem, true);
    }

    public TableItem[] getItems() {
        return table.getItems();
    }

    public void setContext(final JavaElementLocation location) {
        for (final TableItem item : table.getItems()) {
            final IProvider provider = (IProvider) ((Control) item.getData()).getData();
            final boolean availableForLocation = provider.isAvailableForLocation(location);
            setChecked(item, availableForLocation);
            setGrayed(item, !availableForLocation);
        }
    }

    public void setChecked(final TableItem tableItem, final boolean checked) {
        tableItem.setChecked(checked);
        final Control control = (Control) tableItem.getData();
        ((GridData) control.getLayoutData()).exclude = !checked;
        control.setVisible(checked);
    }

    public void setGrayed(final TableItem item, final boolean grayed) {
        item.setGrayed(grayed);
        item.setForeground(grayed ? grayColor : blackColor);
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
                event.feedback |= pt.y < bounds.y + bounds.height / 2 ? DND.FEEDBACK_INSERT_BEFORE
                        : DND.FEEDBACK_INSERT_AFTER;
            }
        }

        @Override
        public void drop(final DropTargetEvent event) {
            final TableItem item = (TableItem) event.item;
            int index;
            if (item == null) {
                index = table.getItemCount() - 1;
            } else {
                final Point pt = table.getShell().getDisplay().map(null, table, event.x, event.y);
                final Rectangle bounds = item.getBounds();
                final TableItem[] items = table.getItems();
                index = pt.y < bounds.y + bounds.height / 2 ? 0 : 1;
                for (int i = 0; i < items.length; ++i) {
                    if (items[i] == item) {
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
