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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;

import org.junit.Assert;
import org.junit.Test;

public final class SubclassingProviderTest {

    @Test
    public void testSubclassingProvider() {
        final SubclassingServer server = new SubclassingServer(ServerUtils.getServer(),
                ServerUtils.getUsernameListener());
        final SubclassingProvider provider = new SubclassingProvider(server);

        provider.createControl(ExtDocUtils.getShell(), null);

        for (final IJavaElement element : TestUtils.getDefaultElements()) {
            final TestJavaElementSelection selection = new TestJavaElementSelection(JavaElementLocation.METHOD_BODY,
                    element);
            Assert.assertTrue(provider.selectionChanged(selection));
        }
    }

}
