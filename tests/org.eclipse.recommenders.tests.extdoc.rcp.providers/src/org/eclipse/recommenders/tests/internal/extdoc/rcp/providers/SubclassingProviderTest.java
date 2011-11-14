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
package org.eclipse.recommenders.tests.internal.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.extdoc.transport.SubclassingServer;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.SubclassingProvider;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.extdoc.TestTypeUtils;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public final class SubclassingProviderTest {

    @Test
    public void testSubclassingProvider() {
        final SubclassingServer server = new SubclassingServer(ServerUtils.getServer(),
                ServerUtils.getUsernameListener());
        final SubclassingProvider provider = new SubclassingProvider(server);

        final Composite composite = provider.createComposite(ExtDocUtils.getShell(), null);

        for (final IJavaElement element : TestTypeUtils.getDefaultElements()) {
            for (final JavaElementLocation location : JavaElementLocation.values()) {
                provider.selectionChanged(ExtDocUtils.getSelection(location, element), composite);
            }
        }
    }

}
