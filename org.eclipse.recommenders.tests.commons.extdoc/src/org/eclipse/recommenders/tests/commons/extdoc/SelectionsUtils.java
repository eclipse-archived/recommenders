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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;

public final class SelectionsUtils {

    private static IJavaElementSelection selection;

    private SelectionsUtils() {
    }

    public static IJavaElementSelection getSelection() {
        if (selection == null) {
            selection = new TestJavaElementSelection();
        }
        return selection;
    }

}
