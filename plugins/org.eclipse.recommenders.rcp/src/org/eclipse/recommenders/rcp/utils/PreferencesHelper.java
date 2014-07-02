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
package org.eclipse.recommenders.rcp.utils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class PreferencesHelper {

    public static final String PREFERENCE_PAGE_EXTENTIONPOINT_ID = "org.eclipse.ui.preferencePages"; //$NON-NLS-1$

    public static String createLinkLabelToPreferencePage(String preferencePageID) {
        String text = getNameOfPreferencePage(preferencePageID);

        String categoryID = getCategoryOfPreferencePage(preferencePageID);
        while (categoryID != null) {
            text = getNameOfPreferencePage(categoryID) + " > " + text; //$NON-NLS-1$
            categoryID = getCategoryOfPreferencePage(categoryID);
        }
        if (text == null) {
            return ""; //$NON-NLS-1$
        } else {
            return text;
        }
    }

    private static String getNameOfPreferencePage(String preferencePageID) {
        return getAttributeOfPreferencePage(preferencePageID, "name"); //$NON-NLS-1$
    }

    private static String getCategoryOfPreferencePage(String preferencePageID) {
        return getAttributeOfPreferencePage(preferencePageID, "category"); //$NON-NLS-1$
    }

    private static String getAttributeOfPreferencePage(String preferencePageID, String attribute) {
        if (preferencePageID != null) {
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    PREFERENCE_PAGE_EXTENTIONPOINT_ID);

            if (elements != null) {
                for (IConfigurationElement e : elements) {
                    String configId = e.getAttribute("id"); //$NON-NLS-1$
                    if (preferencePageID.equalsIgnoreCase(configId)) {
                        return e.getAttribute(attribute);
                    }
                }
            }
        }
        return null;
    }

}
