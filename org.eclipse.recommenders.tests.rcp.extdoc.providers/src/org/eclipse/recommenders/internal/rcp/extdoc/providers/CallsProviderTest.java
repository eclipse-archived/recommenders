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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.CallsAdapterTest;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;

import org.junit.Assert;
import org.junit.Test;

public final class CallsProviderTest {

    @Test
    public void testCallsProvider() throws JavaModelException {
        final CallsProvider provider = new CallsProvider(CallsAdapterTest.createProjectServices(), null,
                ServerUtils.getGenericServer());

        provider.createContentComposite(ExtDocUtils.getShell());
        Assert.assertTrue(provider.selectionChanged(ExtDocUtils.getSelection()));
    }
}
