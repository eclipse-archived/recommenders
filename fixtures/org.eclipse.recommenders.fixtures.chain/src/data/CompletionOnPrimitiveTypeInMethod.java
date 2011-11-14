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

public class CompletionOnPrimitiveTypeInMethod {

    private class A {
        public int findMe = 5;

        public int[] findMe2() {
            return new int[1];
        }
    }

    final A useMe = new A();

    public void method() {
        //@start
        final A useMe = new A();
        final int c = <^Space|useMe.findMe .*>
        //@end
        // final A useMe = new A();
        //final int c = useMe.findMe
    }

    public void method2() {
        //@start
        final int c = <^Space|useMe.findMe .*>
        //@end
        //final int c = useMe.findMe
    }
}
