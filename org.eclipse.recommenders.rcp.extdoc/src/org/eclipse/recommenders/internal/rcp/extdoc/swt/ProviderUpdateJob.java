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

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

class ProviderUpdateJob extends WorkspaceJob {

    private final ProvidersTable table;
    private final TableItem item;
    private final IProvider provider;
    private final IJavaElementSelection selection;

    ProviderUpdateJob(final ProvidersTable table, final TableItem item, final IJavaElementSelection selection) {
        super(String.format("Updating %s", ((IProvider) ((Control) item.getData()).getData()).getProviderFullName()));
        super.setPriority(SHORT);
        this.table = table;
        this.item = item;
        provider = (IProvider) ((Control) item.getData()).getData();
        this.selection = selection;
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) {
        try {
            monitor.beginTask("Updating Extended Javadocs", 1);
            final boolean hasContent = provider.selectionChanged(selection);
            new UIJob("Update provider table") {
                @Override
                public IStatus runInUIThread(final IProgressMonitor monitor) {
                    table.setContentVisible(item, hasContent);
                    return Status.OK_STATUS;
                }
            }.schedule();
            return Status.OK_STATUS;
        } finally {
            monitor.done();
        }
    }
}
