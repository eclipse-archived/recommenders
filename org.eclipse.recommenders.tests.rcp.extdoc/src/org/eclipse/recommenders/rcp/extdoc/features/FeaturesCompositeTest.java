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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.tests.rcp.extdoc.TestProvider;
import org.eclipse.recommenders.tests.rcp.extdoc.TestServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.junit.Assert;
import org.junit.Test;

public final class FeaturesCompositeTest {

    @Test
    public void testCreate() {
        final Shell shell = new Shell();
        final Composite parent = new Composite(shell, SWT.NONE);
        final IProvider provider = new TestProvider();
        final IStarsRatingsServer server = new TestServer();
        final Dialog editDialog = new TitleAreaDialog(shell);

        provider.createControl(parent, null);

        final FeaturesComposite composite = FeaturesComposite.create(parent, null, null, provider, server, editDialog);

        Assert.assertNotNull(composite);
    }
}
