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

import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

final class ProvidersTableDropAdapter extends DropTargetAdapter {

    private final Table table;
    private final ProvidersTableDragListener dragListener;
    private final ProviderStore providerStore;

    ProvidersTableDropAdapter(final Table table, final ProvidersTableDragListener dragListener,
            final ProviderStore providerStore) {
        this.table = table;
        this.dragListener = dragListener;
        this.providerStore = providerStore;
    }

    @Override
    public void dragOver(final DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_SCROLL;
        if (event.item == null) {
            event.feedback |= DND.FEEDBACK_INSERT_AFTER;
        } else {
            final Rectangle bounds = ((TableItem) event.item).getBounds();
            final Point point = table.getShell().getDisplay().map(null, table, event.x, event.y);
            event.feedback |= point.y < bounds.y + bounds.height >> 1 ? DND.FEEDBACK_INSERT_BEFORE
                    : DND.FEEDBACK_INSERT_AFTER;
        }
    }

    @Override
    public void drop(final DropTargetEvent event) {
        final int newIndex = getNewIndex(event);
        dropTableItem(dragListener.getDragSourceItem(), newIndex);
    }

    private int getNewIndex(final DropTargetEvent event) {
        final TableItem item = (TableItem) event.item;
        if (item == null) {
            return table.getItemCount();
        }
        int index;
        final Point point = table.getShell().getDisplay().map(null, table, event.x, event.y);
        final Rectangle bounds = item.getBounds();
        final TableItem[] items = table.getItems();
        index = point.y < bounds.y + bounds.height >> 1 ? 0 : 1;
        for (int i = 0; i < items.length; ++i) {
            if (items[i].equals(item)) {
                index += i;
                break;
            }
        }
        return index;
    }

    void dropTableItem(final TableItem dragSourceItem, final int index) {
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

        updateProviderStore(dragSourceItem);
    }

    private void updateProviderStore(final TableItem dragSourceItem) {
        for (int i = 0; i < table.getItemCount(); ++i) {
            if (table.getItem(i).equals(dragSourceItem)) {
                table.remove(i);
                break;
            }
        }
        final int itemCount = table.getItemCount();
        for (int i = 0; i < itemCount; ++i) {
            final TableItem item = table.getItem(i);
            final IProvider provider = (IProvider) ((Control) item.getData()).getData();
            providerStore.setProviderPriority(provider, itemCount - i);
        }
    }
}