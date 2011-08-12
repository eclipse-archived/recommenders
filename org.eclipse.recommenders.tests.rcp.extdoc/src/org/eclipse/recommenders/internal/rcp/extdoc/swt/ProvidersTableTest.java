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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import org.junit.Assert;
import org.junit.Test;

public final class ProvidersTableTest {

    private final ProvidersTable table = new ProvidersTable(ExtDocUtils.getShell(), new ProviderStore());

    @Test
    public void testProvidersTable() {
        table.addProvider(mockProviderComposite(), "Test", null);
        Assert.assertEquals(1, table.getItems().length);

        table.setContext(ExtDocUtils.getSelection());
        final IJavaElementSelection lastSelection = table.getLastSelection();
        Assert.assertEquals(ExtDocUtils.getSelection(), lastSelection);

        for (final TableItem item : table.getItems()) {
            ProvidersTable.setContentVisible(item, true, true);
        }
    }

    @Test
    public void testGetPreferenceId() {
        final String prefId = ProvidersTable.getPreferenceId(ExtDocUtils.getTestProvider(), ExtDocUtils.getSelection()
                .getElementLocation());
        Assert.assertEquals("provider779624340METHOD_BODY", prefId);
    }

    @Test
    public void testSetChecked() {
        // table.setChecked(preferenceId, isChecked);
    }

    static Composite mockProviderComposite() {
        final Composite control = new Composite(ExtDocUtils.getShell(), SWT.NONE);
        control.setLayoutData(new GridData());
        control.setData(ExtDocUtils.getTestProvider());
        return control;
    }

}
