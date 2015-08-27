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
package org.eclipse.recommenders.utils.rcp.preferences;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PreferencesPagesTest {

    private static final String PREFPAGE_ID_NON_EXISTING = "invalid";
    private static final String PREFPAGE_ID_WORKBENCH = "org.eclipse.ui.preferencePages.Workbench";
    private static final String PREFPAGE_ID_EDITORS = "org.eclipse.ui.preferencePages.Editors";
    private static final String PREFPAGE_ID_COLORS_AND_FONTS = "org.eclipse.ui.preferencePages.ColorsAndFonts";

    @Test
    public void emptyLinkStringForEmptyPrefPageID() {
        assertThat(PreferencePages.createLinkLabelToPreferencePage(""), isEmptyString());
    }

    @Test
    public void emptyLinkStringForNonExistingPrefPageID() {
        assertThat(PreferencePages.createLinkLabelToPreferencePage(PREFPAGE_ID_NON_EXISTING), isEmptyString());
    }

    @Test
    public void correctLinkStringForPrefPageWithoutTopCategory() {
        assertThat(PreferencePages.createLinkLabelToPreferencePage(PREFPAGE_ID_WORKBENCH), is(equalTo("General")));
    }

    @Test
    public void correctLinkStringForPrefPageWithOneTopCategory() {
        assertThat(PreferencePages.createLinkLabelToPreferencePage(PREFPAGE_ID_EDITORS),
                is(equalTo("General > Editors")));
    }

    @Test
    public void correctLinkStringForPrefPageWithMoreTopCategory() {
        assertThat(PreferencePages.createLinkLabelToPreferencePage(PREFPAGE_ID_COLORS_AND_FONTS),
                is(equalTo("General > Appearance > Colors and Fonts")));
    }
}
