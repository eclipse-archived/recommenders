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
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.recommenders.tests.rcp.extdoc.TestProvider;
import org.eclipse.recommenders.tests.rcp.extdoc.UnitTestSuite;

import org.junit.Assert;
import org.junit.Test;

public final class DeleteDialogTest {

    @Test
    public void testCommentsDialog() {
        final TestProvider provider = new TestProvider();
        provider.createControl(UnitTestSuite.getShell(), null);

        final DeleteDialog dialog = new DeleteDialog(provider, new Object(), "TestObject");
        // dialog.open();
        Assert.assertTrue(true);
    }

}
