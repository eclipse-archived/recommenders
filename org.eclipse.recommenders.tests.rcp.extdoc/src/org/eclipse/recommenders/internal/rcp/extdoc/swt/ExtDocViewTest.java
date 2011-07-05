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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.tests.rcp.extdoc.TestProvider;
import org.eclipse.recommenders.tests.rcp.extdoc.UnitTestSuite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.junit.Assert;
import org.junit.Test;

public final class ExtDocViewTest {

    @Test
    public void testExtDocView() {
        final ProviderStore store = new ProviderStore() {
            @Override
            public ImmutableList<IProvider> getProviders() {
                final Builder<IProvider> builder = ImmutableList.builder();
                builder.add(new TestProvider());
                return builder.build();
            }
        };

        final ExtDocView view = new ExtDocView(store);
        view.createPartControl(UnitTestSuite.getShell());
        // view.selectionChanged(UnitTestSuite.getSelection());

        Assert.assertTrue(true);
    }

}
