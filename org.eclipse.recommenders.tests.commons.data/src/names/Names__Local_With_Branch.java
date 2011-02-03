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
package names;

import acme.Button;

public class Names__Local_With_Branch {

    void test(final boolean flag) {
        Button local = null;
        if (flag) {
            local = new Button();
        } else {
            local = new Button();
        }
        local.hashCode();
    }
}
