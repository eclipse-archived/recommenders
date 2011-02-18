/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionViaStaticArrayInMethod {

    public AtomicBoolean findMe = new AtomicBoolean();

    public static CompletionViaStaticArrayInMethod useUs[] = { new CompletionViaStaticArrayInMethod(),
            new CompletionViaStaticArrayInMethod() };

    public static void method1() {
		final AtomicBoolean c = <^Space>
        /* calling context --> static
         * expected type --> AtomicBoolean
         * expected completion --> useUs[i].findMe
         * variable name --> c
         */
    }
}
