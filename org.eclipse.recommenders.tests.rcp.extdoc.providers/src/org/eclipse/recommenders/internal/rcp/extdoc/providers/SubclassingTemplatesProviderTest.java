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
import org.eclipse.recommenders.tests.commons.extdoc.SelectionsUtils;
import org.junit.Test;

public final class SubclassingTemplatesProviderTest {

    @Test
    public void testSubclassingTemplatesProvider() {
        final SubclassingTemplatesProvider provider = new SubclassingTemplatesProvider(new SubclassingServer());
        provider.selectionChanged(SelectionsUtils.getSelection());
    }
}
