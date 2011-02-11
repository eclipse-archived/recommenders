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

public class Tracing__Calls_To_Several_Parameters {

    void __test(final Button s1, final Button s2, final Button s3) {
        s1.equals(null);
        //
        s2.foo1();
        s2.hashCode();
        //
        s3.equals(null);
        s3.notify();
        s3.toString();
    }
}
