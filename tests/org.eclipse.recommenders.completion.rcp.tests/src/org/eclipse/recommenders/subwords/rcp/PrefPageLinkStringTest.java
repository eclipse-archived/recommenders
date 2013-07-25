/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.subwords.rcp;

import static junit.framework.Assert.assertEquals;

import org.eclipse.recommenders.internal.subwords.rcp.PreferencesHelper;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class PrefPageLinkStringTest {

    private static final String PREFPAGE_ID_EMPTY = "";
    private static final String PREFPAGE_ID_NON_EXISTING = "non.existing";
    private static final String PREFPAGE_ID_JAVA_BASE = "org.eclipse.jdt.ui.preferences.JavaBasePreferencePage";
    private static final String PREFPAGE_ID_EDITORS = "org.eclipse.ui.preferencePages.Editors";
    private static final String PREFPAGE_ID_SPELLING = "org.eclipse.ui.editors.preferencePages.Spelling";

    @Test
    public void emptyLinkStringForEmptyPrefPageID() {
        assertEquals("", PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_EMPTY));
    }

    @Test
    public void emptyLinkStringForNonExistingPrefPageID() {
        assertEquals("", PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_NON_EXISTING));
    }

    @Test
    public void correctLinkStringForPrefPageWithoutTopCategory() {
        assertEquals("Java", PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_JAVA_BASE));
    }

    @Test
    public void correctLinkStringForPrefPageWithOneTopCategory() {
        assertEquals("General > Editors", PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_EDITORS));
    }

    @Test
    public void correctLinkStringForPrefPageWithMoreTopCategory() {
        assertEquals("General > Editors > Text Editors > Spelling",
                PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_SPELLING));
    }
}
