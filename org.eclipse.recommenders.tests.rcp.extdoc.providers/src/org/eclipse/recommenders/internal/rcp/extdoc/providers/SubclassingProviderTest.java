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

import org.junit.Assert;
import org.junit.Test;

public final class SubclassingProviderTest {

    @Test
    public void testSubclassingProvider() {
        final SubclassingProvider provider = new SubclassingProvider(new SubclassingServer());

        Assert.assertTrue(true);
    }

}
