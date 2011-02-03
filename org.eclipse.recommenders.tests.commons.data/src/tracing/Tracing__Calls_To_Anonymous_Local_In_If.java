/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package tracing;

import acme.Composite;
import acme.GridData;

public class Tracing__Calls_To_Anonymous_Local_In_If {

    private int style;
    private int FLAT;
    private Composite defaultParent;

    Composite __test(final boolean condition) {
        if (style == FLAT) {
            final Composite parent = new Composite(defaultParent, FLAT);
            parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            return parent;
        }
        return defaultParent;
    }
}
