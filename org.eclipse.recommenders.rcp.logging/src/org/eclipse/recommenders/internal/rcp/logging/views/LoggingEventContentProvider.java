/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging.views;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.internal.rcp.logging.model.LoggingEventManager;
import org.eclipse.recommenders.internal.rcp.logging.model.LoggingEventManagerEvent;
import org.eclipse.recommenders.internal.rcp.logging.model.LoggingEventManagerListener;
import org.eclipse.swt.widgets.Display;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LoggingEventContentProvider implements LoggingEventManagerListener, ILazyContentProvider {

    private TableViewer viewer;
    private boolean autoScroll = true;
    private int lastCount = 0;

    public LoggingEventContentProvider(final TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void loggingEventsChanged(final LoggingEventManagerEvent event) {
        // If this is the UI thread, then make the change
        if (Display.getCurrent() != null) {
            updateViewer(event);
            return;
        }

        // otherwise redirect to execute on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateViewer(event);
            }
        });
    }

    private void updateViewer(final LoggingEventManagerEvent event) {
        if (viewer == null || viewer.getControl().isDisposed()) {
            return;
        }
        final int count = LoggingEventManager.getManager().getEventCount();
        viewer.setItemCount(count);
        if (count < lastCount) {
            // we've stripped down the list, we
            // need to refresh the whole viewer
            viewer.refresh();
        }
        lastCount = count;
        if (event.getItemsAdded().length > 0 && autoScroll) {
            viewer.getTable().showItem(viewer.getTable().getItem(count - 1));
        }
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = (TableViewer) viewer;
    }

    public void toggleAutoScroll() {
        autoScroll = !autoScroll;
        final int size = LoggingEventManager.getManager().getEventCount();
        if (autoScroll && size > 0) {
            viewer.getTable().showItem(viewer.getTable().getItem(size - 1));
        }
    }

    public boolean getAutoScroll() {
        return autoScroll;
    }

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public void updateElement(final int index) {
        final ILoggingEvent event = LoggingEventManager.getManager().getEvent(index);
        if (event != null) {
            viewer.replace(event, index);
        }
    }
}
