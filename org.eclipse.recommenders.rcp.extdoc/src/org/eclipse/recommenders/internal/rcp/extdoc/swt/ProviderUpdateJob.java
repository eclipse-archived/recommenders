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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

class ProviderUpdateJob extends Job {

    private static final Image ICON_LOADING = ExtDocPlugin.getIcon("lcl16/loading.png");

    private static Set<ProviderUpdateJob> active = new HashSet<ProviderUpdateJob>();

    private final ProvidersTable table;
    private final TableItem item;
    private final IProvider provider;
    private final IJavaElementSelection selection;

    ProviderUpdateJob(final ProvidersTable table, final TableItem item, final IJavaElementSelection selection) {
        super(String.format("Updating %s", ((IProvider) ((Control) item.getData()).getData()).getProviderFullName()));
        super.setPriority(LONG);
        active.add(this);

        this.table = table;
        this.item = item;
        provider = (IProvider) ((Control) item.getData()).getData();
        this.selection = selection;

        item.setImage(ICON_LOADING);
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        try {
            monitor.beginTask("Updating Extended Javadocs", 1);
            updateProvider();
            return Status.OK_STATUS;
        } finally {
            monitor.done();
            active.remove(this);
        }
    }

    private void updateProvider() {
        final boolean hasContent = provider.selectionChanged(selection);
        new UIJob("Update provider table") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!item.isDisposed()) {
                    table.setContentVisible(item, hasContent);
                    item.setImage((Image) item.getData("image"));
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public static void cancelActiveJobs() {
        for (final ProviderUpdateJob job : active) {
            job.cancel();
        }
        active.clear();
    }
}
