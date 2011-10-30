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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.UpdateService.AbstractUpdateJob;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

class ViewProviderUpdateJob extends AbstractUpdateJob {

    private static final Image ICON_LOADING = ExtDocPlugin.getIcon("lcl16/loading.gif");

    private final ProvidersTable table;
    private final TableItem item;
    private final Composite composite;
    private final IProvider provider;
    private final IJavaElementSelection selection;

    private boolean hasContent;

    ViewProviderUpdateJob(final ProvidersTable table, final TableItem item, final IJavaElementSelection selection) {
        this.table = table;
        this.item = item;
        composite = (Composite) item.getData();
        provider = (IProvider) composite.getData();
        this.selection = selection;

        item.setImage(ICON_LOADING);
    }

    @Override
    public void run() {
        try {
            hideProvider();
            hasContent = provider.selectionChanged(selection, composite);
        } catch (final Exception e) {
            ExtDocPlugin.logException(e);
        }
    }

    @Override
    public void finishSuccessful() {
        displayProvider(hasContent);
    }

    @Override
    public void handleTimeout() {
        if (!provider.hideOnTimeout()) {
            super.displayTimeoutMessage(provider.resolveContentComposite(composite));
            displayProvider(true);
        }
    }

    private void hideProvider() {
        final UIJob job = new UIJob("Update provider table") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!item.isDisposed()) {
                    table.setContentVisible(item, false, false);
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void displayProvider(final boolean display) {
        final UIJob job = new UIJob("Update provider table") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!item.isDisposed()) {
                    table.setContentVisible(item, display, true);
                    item.setImage((Image) item.getData("image"));
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
