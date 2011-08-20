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

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

final class ProvidersTableDragListener implements DragSourceListener {

    private final Table table;
    private TableItem dragSourceItem;

    ProvidersTableDragListener(final Table table) {
        this.table = table;
    }

    @Override
    public void dragStart(final DragSourceEvent event) {
        event.doit = true;
        dragSourceItem = table.getSelection()[0];
    }

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

    TableItem getDragSourceItem() {
        return dragSourceItem;
    }
}