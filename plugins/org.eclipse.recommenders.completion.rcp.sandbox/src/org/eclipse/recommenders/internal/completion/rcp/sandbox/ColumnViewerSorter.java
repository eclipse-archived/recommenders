/**
 * Copyright (c) 2006, 2007 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public abstract class ColumnViewerSorter extends ViewerComparator {
    public static final int ASC = 1;

    public static final int NONE = 0;

    public static final int DESC = -1;

    private int direction = 0;

    private final TableViewerColumn column;

    private final ColumnViewer viewer;

    public ColumnViewerSorter(ColumnViewer viewer, TableViewerColumn column) {
        this.column = column;
        this.viewer = viewer;
        this.column.getColumn().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ColumnViewerSorter.this.viewer.getComparator() != null) {
                    if (ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this) {
                        int tdirection = ColumnViewerSorter.this.direction;

                        if (tdirection == ASC) {
                            setSorter(ColumnViewerSorter.this, DESC);
                        } else if (tdirection == DESC) {
                            setSorter(ColumnViewerSorter.this, ASC);
                        }
                    } else {
                        setSorter(ColumnViewerSorter.this, ASC);
                    }
                } else {
                    setSorter(ColumnViewerSorter.this, ASC);
                }
            }
        });
    }

    public void setSorter(ColumnViewerSorter sorter, int direction) {
        if (direction == NONE) {
            column.getColumn().getParent().setSortColumn(null);
            column.getColumn().getParent().setSortDirection(SWT.NONE);
            viewer.setComparator(null);
        } else {
            column.getColumn().getParent().setSortColumn(column.getColumn());
            sorter.direction = direction;

            if (direction == ASC) {
                column.getColumn().getParent().setSortDirection(SWT.UP);
            } else {
                column.getColumn().getParent().setSortDirection(SWT.DOWN);
            }

            if (viewer.getComparator() == sorter) {
                viewer.refresh();
            } else {
                viewer.setComparator(sorter);
            }

        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        return direction * doCompare(viewer, e1, e2);
    }

    protected abstract int doCompare(Viewer viewer, Object e1, Object e2);
}
