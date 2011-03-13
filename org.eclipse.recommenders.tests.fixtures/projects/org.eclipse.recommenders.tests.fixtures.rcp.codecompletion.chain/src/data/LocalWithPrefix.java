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
package data;

import java.util.concurrent.atomic.AtomicBoolean;

//call chain 1 ok
public class LocalWithPrefix {

    public AtomicBoolean findMe = new AtomicBoolean();

    public static void method1() {
        final LocalWithPrefix useMe = new LocalWithPrefix();
        final AtomicBoolean c = use<@Ignore^Space>
        /*
         * calling context --> static
         * expected type --> AtomicBoolean
         * expected completion --> [use]Me.findMe
         * variable name --> c
         */
    }
}
