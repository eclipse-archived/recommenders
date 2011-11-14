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
package org.eclipse.recommenders.extdoc.rcp.preferences;

import org.eclipse.recommenders.extdoc.rcp.preferences.ExtDocPreferencePage;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public final class ExtDocPreferencePageTest {

    @Test
    public void testExtDocPreferencePage() {
        final ExtDocPreferencePage page = new ExtDocPreferencePage();
        page.createControl(new Composite(ExtDocUtils.getShell(), SWT.NONE));
        page.createFieldEditors();
    }

}
