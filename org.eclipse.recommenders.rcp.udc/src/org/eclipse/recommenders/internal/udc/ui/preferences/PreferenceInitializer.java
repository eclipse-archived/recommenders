/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public PreferenceInitializer() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initializeDefaultPreferences() {
        PreferenceUtil.setExpressions(new String[] { ".*" }, PackagePreferences.includExpressions);
    }

}
