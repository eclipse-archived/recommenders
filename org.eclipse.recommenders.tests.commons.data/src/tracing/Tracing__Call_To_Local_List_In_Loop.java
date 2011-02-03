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

import java.util.LinkedList;
import java.util.List;

import acme.Button;

public class Tracing__Call_To_Local_List_In_Loop {

    void __test() {
        final List<Button> buttons = new LinkedList<Button>();
        buttons.add(new Button());
        buttons.add(new Button());
        loop(buttons);
    }

    private void loop(final List<Button> buttons) {
        for (final Button b : buttons) {
            b.foo1();
        }
    }
}
