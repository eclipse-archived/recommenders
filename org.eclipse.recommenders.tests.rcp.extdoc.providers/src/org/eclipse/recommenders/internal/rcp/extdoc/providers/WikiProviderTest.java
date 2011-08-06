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

import org.eclipse.recommenders.server.extdoc.WikiServer;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.junit.Assert;
import org.junit.Test;

public final class WikiProviderTest {

    @Test
    public void testWikiProvider() {
        final WikiServer server = new WikiServer(ServerUtils.getServer(), ServerUtils.getUsernameListener());
        final WikiProvider provider = new WikiProvider(server);
        provider.createContentComposite(new Shell());

        final Composite composite = provider.createComposite(ExtDocUtils.getShell(), null);
        Assert.assertTrue(provider.selectionChanged(ExtDocUtils.getSelection(), composite));
    }
}
