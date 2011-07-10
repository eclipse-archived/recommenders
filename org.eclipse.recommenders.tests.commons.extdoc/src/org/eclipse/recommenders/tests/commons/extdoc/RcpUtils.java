/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.extdoc;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class RcpUtils {

    private static Shell shell;

    private RcpUtils() {
    }

    public static Shell getShell() {
        if (shell == null) {
            shell = new MyShell(Display.getDefault());
        }
        return shell;
    }

    private static final class MyShell extends Shell {

        MyShell(final Display display) {
            super(display);
        }

        @Override
        protected void checkSubclass() {
        }

    }

}
