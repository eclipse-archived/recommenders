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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JavadocProviderTest {

    @Test
    public void testJavadocProvider() {
        final Shell shell = new Shell();

        final JavadocProvider provider = new JavadocProvider();
        final IViewSite viewSite = Mockito.mock(IViewSite.class);
        Mockito.when(viewSite.getShell()).thenReturn(shell);
        // provider.createControl(shell, viewSite);

        Assert.assertTrue(true);
    }

}
