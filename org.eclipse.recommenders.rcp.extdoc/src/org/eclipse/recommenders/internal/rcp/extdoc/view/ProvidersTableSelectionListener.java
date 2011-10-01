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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.UpdateService;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

final class ProvidersTableSelectionListener implements Listener {

    private final ProvidersTable table;
    private final UpdateService updateService;

    ProvidersTableSelectionListener(final ProvidersTable table, final UpdateService updateService) {
        this.table = table;
        this.updateService = updateService;
    }

    @Override
    public void handleEvent(final Event event) {
        final TableItem tableItem = (TableItem) event.item;
        final Control control = (Control) tableItem.getData();
        if (event.detail == SWT.CHECK) {
            checkProvider(tableItem, control);
        } else if (!tableItem.getGrayed()) {
            scrollToProvider(control);
        }
    }

    private void checkProvider(final TableItem tableItem, final Control control) {
        final IJavaElementSelection lastSelection = table.getLastSelection();
        final String preferenceId = ProvidersTable.getPreferenceId((IProvider) control.getData(),
                lastSelection.getElementLocation());
        table.setChecked(preferenceId, tableItem.getChecked());
        if (tableItem.getGrayed()) {
            if (tableItem.getChecked()) {
                updateService.schedule(new ProviderUpdateJob(table, tableItem, lastSelection));
                updateService.invokeAll();
            }
        } else {
            table.setContentVisible(tableItem, tableItem.getChecked(), true);
        }
    }

    private static void scrollToProvider(final Control control) {
        ((ScrolledComposite) control.getParent().getParent()).setOrigin(control.getLocation());
    }

}