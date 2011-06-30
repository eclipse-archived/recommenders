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
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.List;

import org.eclipse.recommenders.rcp.extdoc.IProvider;

import org.junit.Test;

import junit.framework.Assert;

public final class ProviderStoreTest {

    @Test
    public void testGetProviders() {
        final ProviderStore store = new ProviderStore();
        final List<IProvider> providers = store.getProviders();

        Assert.assertEquals(6, providers.size());

        for (final IProvider provider : providers) {
            Assert.assertTrue(!provider.getProviderName().isEmpty());
            Assert.assertTrue(!provider.getProviderFullName().isEmpty());
            Assert.assertNotNull(provider.getIcon());
        }
    }

    @Test
    public void testSetProviderPriority() {
        final ProviderStore store = new ProviderStore();
        final List<IProvider> providers = store.getProviders();

        int priority = 0;
        for (final IProvider provider : providers) {
            store.setProviderPriority(provider, ++priority);
            Assert.assertEquals(provider, store.getProviders().get(0));
        }
    }
}
