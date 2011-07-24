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
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import org.junit.Test;

public final class ProvidersTableSelectionListenerTest {

    @Test
    public void testHandleEvent() {
        final Event event = new Event();
        final ProvidersTable table = new ProvidersTable(ExtDocUtils.getShell(), new ProviderStore());
        final TableItem item = table.addProvider(ProvidersTableTest.mockProviderControl(), "Test", null, true);
        table.setContext(ExtDocUtils.getSelection());
        event.item = item;
        final ProvidersTableSelectionListener listener = new ProvidersTableSelectionListener(table);

        listener.handleEvent(event);

        event.detail = SWT.CHECK;
        listener.handleEvent(event);
        item.setGrayed(false);
        listener.handleEvent(event);
    }

}
