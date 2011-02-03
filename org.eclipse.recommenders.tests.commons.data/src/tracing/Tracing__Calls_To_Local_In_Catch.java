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

@SuppressWarnings("unused")
public class Tracing__Calls_To_Local_In_Catch {

    void __test() {
        final String c = new String("");
        try {
            c.split("");
            throw new IllegalArgumentException();
        } catch (final Exception e) {
        }
    }

    private void throwException() throws IllegalArgumentException {
        throw new IllegalAccessError("");
    }
}
