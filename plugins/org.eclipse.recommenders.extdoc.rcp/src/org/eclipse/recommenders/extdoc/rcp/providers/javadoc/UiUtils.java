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
package org.eclipse.recommenders.extdoc.rcp.providers.javadoc;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class UiUtils {

    private UiUtils() {
    }

    /**
     * @param composite
     *            The composite for which all children will be disposed.
     */
    public static void disposeChildren(final Composite composite) {
        if (composite.isDisposed()) {
            return;
        }
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

    public static void layoutParents(final Composite composite) {
        for (Composite parent = composite; parent != null; parent = parent.getParent()) {
            // TODO: REVIEW MB: Johannes, this is confusing me. why is the
            // parentsParentsParent needed? create a separate method for this?
            final Composite theParentsParent = parent.getParent();
            final Composite theParentsParentsParent = theParentsParent.getParent();
            if (theParentsParentsParent == null || parent instanceof ScrolledComposite) {
                theParentsParent.layout(true, true);
                break;
            }
        }
    }

}
