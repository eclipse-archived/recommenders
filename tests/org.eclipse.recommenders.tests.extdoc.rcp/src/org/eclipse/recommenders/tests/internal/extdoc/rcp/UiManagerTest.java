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
package org.eclipse.recommenders.tests.internal.extdoc.rcp;

import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.extdoc.rcp.UiManager;
import org.eclipse.recommenders.internal.extdoc.rcp.view.ExtDocView;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public final class UiManagerTest {

    @Test
    public void testSelectionChanged() {
        final ExtDocView view = Mockito.mock(ExtDocView.class);

        final IJavaElementSelection selection = ExtDocUtils.getSelection();

        final UiManager manager = new UiManager(view, null, null);
        manager.selectionChanged(selection);
        manager.selectionChanged(selection);

        Mockito.verify(view, Mockito.times(1)).selectionChanged(Matchers.any(IJavaElementSelection.class));
    }
}
