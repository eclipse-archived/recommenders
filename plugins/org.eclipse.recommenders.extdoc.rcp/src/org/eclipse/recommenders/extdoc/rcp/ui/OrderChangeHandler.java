/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.extdoc.rcp.ui;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

public class OrderChangeHandler extends DropTargetAdapter implements DragSourceListener {

    public interface OrderChangedListener {
        public void orderChanged(int oldIndex, int newIndex);
    }

    private final Table table;
    private TableItem dragSourceItem;

    private final List<OrderChangedListener> listeners = newArrayList();

    public static OrderChangeHandler enable(Table table) {
        return new OrderChangeHandler(table);
    }

    private OrderChangeHandler(Table table) {
        this.table = table;

        final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        DragSource source = new DragSource(table, operations);
        source.setTransfer(types);
        source.addDragListener(this);

        DropTarget target = new DropTarget(table, operations);
        target.setTransfer(types);
        target.addDropListener(this);
    }

    public void addListener(OrderChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OrderChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyAll(int oldIndex, int newIndex) {
        for (OrderChangedListener listener : listeners) {
            listener.orderChanged(oldIndex, newIndex);
        }
    }

    @Override
    public void dragStart(final DragSourceEvent event) {
        TableItem[] selection = table.getSelection();
        dragSourceItem = selection[0];
    }

    @Override
    public void dragSetData(final DragSourceEvent event) {
        event.data = dragSourceItem.getText();
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        table.deselectAll();
        dragSourceItem = null;
    }

    @Override
    public void dragOver(final DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_INSERT_BEFORE;
    }

    @Override
    public void drop(final DropTargetEvent event) {
        boolean isDraggingFromWithinTable = dragSourceItem != null;
        if (isDraggingFromWithinTable) {
            Widget dropTargetItem = event.item;

            int selIndex = findIndex(dragSourceItem, table);
            int dropIndex = findIndex(dropTargetItem, table);

            if (dropIndex > selIndex) {
                dropIndex--;
            }

            if (dropIndex != selIndex) {
                notifyAll(selIndex, dropIndex);
            }
        } else {
            System.out.println("sorry, dragged from outside");
        }
    }

    private int findIndex(Widget dropTarget, Table table) {
        int index = 0;
        for (TableItem item : table.getItems()) {
            if (item.equals(dropTarget)) {
                return index;
            }
            index++;
        }
        return index;
    }
}