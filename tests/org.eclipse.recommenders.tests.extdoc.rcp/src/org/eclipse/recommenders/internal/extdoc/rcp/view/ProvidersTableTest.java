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
package org.eclipse.recommenders.internal.extdoc.rcp.view;

import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.extdoc.rcp.ProviderStore;
import org.eclipse.recommenders.internal.extdoc.rcp.ProvidersComposite;
import org.eclipse.recommenders.internal.extdoc.rcp.UpdateService;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.junit.Test;

public final class ProvidersTableTest {

    @Test
    public void testProvidersTable() {
        final ProvidersTable table = new ProvidersTable(ExtDocUtils.getShell(), new ProviderStore(),
                new UpdateService());
        table.setProvidersComposite(new ProvidersComposite(ExtDocUtils.getShell(), ExtDocUtils.getWorkbenchWindow()));
        table.addProvider(mockProviderComposite(), "Test", null);
        Assert.assertEquals(1, table.getItems().length);

        table.setContext(ExtDocUtils.getSelection());
        final IJavaElementSelection lastSelection = table.getLastSelection();
        Assert.assertEquals(ExtDocUtils.getSelection(), lastSelection);

        for (final TableItem item : table.getItems()) {
            table.setContentVisible(item, true, true);
        }
    }

    @Test
    public void testGetPreferenceId() {
        final String prefId = ProvidersTable.getPreferenceId(ExtDocUtils.getTestProvider(), ExtDocUtils.getSelection()
                .getElementLocation());
        // TODO XXX MB: WTF???
        Assert.assertEquals("provider-477934950METHOD_BODY", prefId);
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
