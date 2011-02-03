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

@SuppressWarnings("unused")
public class Tracing__Calls_To_Local_Surrounded_By_Primitive_Ops {

    private int style;
    private static int FLAT;
    private Composite defaultParent;
    Composite exception;

    void __test(final boolean condition) {
        loadExceptionText();
        int i = FLAT;
        i++;
        final GridData gd = new GridData(i);
        gd.exclude = true;
        exception.setLayoutData(gd);
    }

    private void loadExceptionText() {
    }
}
