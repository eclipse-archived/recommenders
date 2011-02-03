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

public class Tracing__Call_From_Inner_Class_To_Private_Method_Of_Enclosing_Class {

    public class InnerClass {

        void __test() {
            callToPrivateMethodOfEnclosingClass();
        }
    }

    private final Button b = new Button();

    private void callToPrivateMethodOfEnclosingClass() {
        b.foo1();
    }
}
