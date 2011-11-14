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

import acme.Button;

public class Tracing__Calls_To_Aliased_Local {

    public int __test(final Helper h) {
        Button uri1 = h.getButton();
        if (uri1 == null) {
            uri1 = new Button();
        }
        final Button uri2 = h.getButton();
        // if (uri2 == null)
        // {
        // uri2 = new Button();
        // }
        final int result = uri1.compareTo(uri2);
        return result;
    }
}
