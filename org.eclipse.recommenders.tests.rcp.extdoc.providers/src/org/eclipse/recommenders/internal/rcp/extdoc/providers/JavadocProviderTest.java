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

import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;

import org.junit.Test;
import org.mockito.Mockito;

public final class JavadocProviderTest {

    @Test
    public void testJavadocProvider() {
        final Shell shell = ExtDocUtils.getShell();

        final JavadocProvider provider = new JavadocProvider(ServerUtils.getGenericServer());
        final IViewSite viewSite = Mockito.mock(IViewSite.class);
        Mockito.when(viewSite.getShell()).thenReturn(shell);

        final IWorkbenchWindow workbenchWindow = Mockito.mock(IWorkbenchWindow.class);
        Mockito.when(viewSite.getWorkbenchWindow()).thenReturn(workbenchWindow);
        final IPartService partService = Mockito.mock(IPartService.class);
        Mockito.when(workbenchWindow.getPartService()).thenReturn(partService);

        // provider.createControl(shell, viewSite);
    }

}
