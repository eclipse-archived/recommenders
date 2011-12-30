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

public class Tracing__SimpleIfWithDelegate {

    public void __test(final boolean flag) {
        final Button b = new Button();
        b.foo1();
        if (flag) {
            delegate(b);
        }
        b.foo3();
    }

    private void delegate(final Button button) {
        button.foo2();
    }
}
