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
package org.eclipse.recommenders.tests.commons.selection.ui;

import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;

public final class SelectionObserver implements IExtendedSelectionListener {

    private IJavaElementSelection lastContext;

    @Override
    public void update(final IJavaElementSelection context) {
        lastContext = context;
        // Always call toString to ensure no NPE will occur.
        context.toString();
    }

    protected IJavaElementSelection getLastContext() {
        return lastContext;
    }

}
