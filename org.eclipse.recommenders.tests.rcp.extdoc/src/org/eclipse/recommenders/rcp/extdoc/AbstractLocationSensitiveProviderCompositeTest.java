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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.commons.extdoc.TestTypeUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.junit.Assert;
import org.junit.Test;

public final class AbstractLocationSensitiveProviderCompositeTest {

    private final IProvider provider = new AbstractLocationSensitiveTitledProvider() {
        @Override
        protected Composite createContentComposite(final Composite parent) {
            return new Composite(ExtDocUtils.getShell(), SWT.NONE);
        }
    };

    @Test
    public void testProvidersComposite() {
        final Composite composite = provider.createComposite(ExtDocUtils.getShell(), null);
        for (final JavaElementLocation location : JavaElementLocation.values()) {
            provider.selectionChanged(new TestJavaElementSelection(location, TestTypeUtils.getDefaultJavaType()),
                    composite);
            Assert.assertTrue(location == JavaElementLocation.PACKAGE_DECLARATION
                    || provider.isAvailableForLocation(location));
        }
    }

    public void testNullLocation() {
        final Composite composite = provider.createComposite(ExtDocUtils.getShell(), null);
        Assert.assertFalse(provider.selectionChanged(new TestJavaElementSelection(null, null), composite));
    }

}
