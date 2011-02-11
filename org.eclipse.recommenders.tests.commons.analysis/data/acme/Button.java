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
package acme;

public class Button implements Comparable<Button> {

    public static Button make() {
        return new Button();
    }

    public void foo1() {
    }

    public void foo2() {
    }

    public void foo3() {
    }

    public void addListener(final IListener listener) {
    }

    @Override
    public int compareTo(final Button other) {
        return 0;
    }
}
