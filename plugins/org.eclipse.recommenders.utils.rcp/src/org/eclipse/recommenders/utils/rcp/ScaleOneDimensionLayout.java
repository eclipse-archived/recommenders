/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Layouts all children to have the same width and height as the parent
 * composite. For computing size of the component only the dimension given as
 * parameter to the constructor will be used. <br>
 */
public class ScaleOneDimensionLayout extends Layout {

    private final int dimension;

    /**
     * @param dimension
     *            Dimension which should be scaled, either SWT.VERTICAL or
     *            SWT.HORIZONTAL
     */
    public ScaleOneDimensionLayout(final int dimension) {
        this.dimension = dimension;
    }

    @Override
    protected Point computeSize(final Composite composite, final int wHint, final int hHint, final boolean flushCache) {
        final Control[] children = composite.getChildren();
        int max = 0;
        for (final Control child : children) {
            final Point childSize = child.computeSize(wHint, hHint);
            max = Math.max(max, dimension == SWT.HORIZONTAL ? childSize.x : childSize.y);
        }
        if (dimension == SWT.HORIZONTAL) {
            return new Point(max, 0);
        } else {
            return new Point(0, max);
        }
    }

    @Override
    protected void layout(final Composite composite, final boolean flushCache) {
        final Control[] children = composite.getChildren();
        final Rectangle bounds = composite.getClientArea();
        for (final Control child : children) {
            child.setBounds(bounds);
        }
    }
}
