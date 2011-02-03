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

public class Tracing__Hierarchy_Subclass extends Tracing__Hierarchy_Rootclass {

    @Override
    public void __test(final Button c) {
        super.__test(c);
        c.foo1();
        indirection1(c);
    }

    void indirection1(final Button c) {
        c.foo2();
        indirection2(c);
    }

    void indirection2(final Button x) {
        x.foo3();
    }
}
