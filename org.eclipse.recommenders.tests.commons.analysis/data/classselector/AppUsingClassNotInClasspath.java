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
package classselector;

import org.eclipse.swt.widgets.Button;

public abstract class AppUsingClassNotInClasspath {

    @SuppressWarnings("unused")
    public void __test() {
        final Button button = new Button(null, 0);
    }
}
