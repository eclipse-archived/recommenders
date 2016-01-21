/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

public final class Constants {

    private Constants() {
        // Not meant to be instantiated
    }

    /**
     * Bundle symbolic name of the o.e.r.calls.rcp bundle.
     */
    public static final String BUNDLE_NAME = "org.eclipse.recommenders.calls.rcp"; //$NON-NLS-1$

    /**
     * Templates completion category id
     */
    public static final String TEMPLATES_CATEGORY_ID = "org.eclipse.recommenders.calls.rcp.proposalCategory.templates"; //$NON-NLS-1$

    /**
     * Preference key determining whether already used methods should be highlighted
     */
    public static final String PREF_HIGHLIGHT_USED_PROPOSALS = "highlight_used_proposals"; //$NON-NLS-1$
}
