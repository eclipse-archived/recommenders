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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.junit.Assert;
import org.junit.Test;

public final class ProvidersTableTest {

    @Test
    public void testExtDocView() {
        final ProvidersTable table = new ProvidersTable(new Composite(new Shell(), SWT.NONE), SWT.NONE,
                new ProviderStore());
        // table.setContext(UnitTestSuite.getSelection());

        Assert.assertTrue(true);
    }

}
