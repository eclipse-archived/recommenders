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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.junit.Assert;
import org.junit.Test;

public final class AbstractProviderCompositeTest {

    @Test
    public void testCreateControl() {
        final AbstractTitledProvider composite = new ProviderComposite();
        final Composite control = composite.createComposite(ExtDocUtils.getShell(), null);

        Assert.assertEquals(3, control.getChildren().length);
        composite.disposeChildren(control);
        Assert.assertEquals(0, control.getChildren().length);
    }

    private static final class ProviderComposite extends AbstractTitledProvider {

        @Override
        public boolean isAvailableForLocation(final JavaElementLocation location) {
            return false;
        }

        @Override
        public boolean updateSelection(final IJavaElementSelection context, final Composite composite) {
            return false;
        }

        @Override
        protected Composite createContentComposite(final Composite parent) {
            return new Composite(parent, SWT.NONE);
        }

    }

}
