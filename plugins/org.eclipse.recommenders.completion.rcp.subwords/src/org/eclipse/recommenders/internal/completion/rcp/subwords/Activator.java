/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    private static final String KEY_ENGINE_DISABLED_ON_FIRST_START = "disabled.on.first.start";
    private static Activator INSTANCE;

    public static Activator getDefault() {
        return INSTANCE;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        disableSubwordsOnFirstStartup();
    }

    private void disableSubwordsOnFirstStartup() {
        IPreferenceStore store = getPreferenceStore();
        if (wasCategoryDisabledOnFirstStartupBefore(store)) {
            String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
            if (!ArrayUtils.contains(excluded, SubwordsCompletionProposalComputer.CATEGORY_ID)) {
                excluded = ArrayUtils.add(excluded, SubwordsCompletionProposalComputer.CATEGORY_ID);
                PreferenceConstants.setExcludedCompletionProposalCategories(excluded);
            }
            store.setValue(KEY_ENGINE_DISABLED_ON_FIRST_START, true);
        }
    }

    private boolean wasCategoryDisabledOnFirstStartupBefore(IPreferenceStore store) {
        return !store.getBoolean(KEY_ENGINE_DISABLED_ON_FIRST_START);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        INSTANCE = null;
    }
}
