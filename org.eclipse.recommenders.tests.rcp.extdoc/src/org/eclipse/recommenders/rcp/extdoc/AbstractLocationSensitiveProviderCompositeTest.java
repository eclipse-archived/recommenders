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
import org.eclipse.recommenders.tests.commons.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.junit.Assert;
import org.junit.Test;

public final class AbstractLocationSensitiveProviderCompositeTest {

    private final AbstractLocationSensitiveProviderComposite composite = new AbstractLocationSensitiveProviderComposite() {
        @Override
        protected Control createContentControl(final Composite parent) {
            return null;
        }
    };

    @Test
    public void testProvidersComposite() {
        for (final JavaElementLocation location : JavaElementLocation.values()) {
            composite.selectionChanged(new TestJavaElementSelection(location, TestUtils.getDefaultJavaType()));
            Assert.assertTrue(location == JavaElementLocation.PACKAGE_DECLARATION
                    || composite.isAvailableForLocation(location));
        }
    }

    public void testNullLocation() {
        Assert.assertFalse(composite.selectionChanged(new TestJavaElementSelection(null, null)));
    }

}
