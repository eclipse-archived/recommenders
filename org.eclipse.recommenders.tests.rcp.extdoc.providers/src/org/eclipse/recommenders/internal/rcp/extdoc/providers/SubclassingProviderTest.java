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

import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.junit.Test;

public final class SubclassingProviderTest {

    @Test
    public void testSubclassingProvider() {
        final SubclassingServer server = new SubclassingServer(ServerUtils.getServer(),
                ServerUtils.getUsernameListener());
        final SubclassingProvider provider = new SubclassingProvider(server);
        provider.selectionChanged(ExtDocUtils.getSelection());
    }

}
