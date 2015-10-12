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
package org.eclipse.recommenders.internal.completion.rcp;

public final class Constants {

    private Constants() {
        // Not meant to be instantiated
    }

    public static final String BUNDLE_NAME = "org.eclipse.recommenders.completion.rcp"; //$NON-NLS-1$

    public static final String DEBUG_COMPLETION_RCP = BUNDLE_NAME + "/debug"; //$NON-NLS-1$

    public static final String EXT_POINT_SESSION_PROCESSORS = "org.eclipse.recommenders.completion.rcp.sessionprocessors"; //$NON-NLS-1$
    public static final String EXT_POINT_COMPLETION_TIPS = "org.eclipse.recommenders.completion.rcp.tips"; //$NON-NLS-1$

    public static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory"; //$NON-NLS-1$
    public static final String JDT_NON_TYPE_CATEGORY = "org.eclipse.jdt.ui.javaNoTypeProposalCategory"; //$NON-NLS-1$
    public static final String JDT_TYPE_CATEGORY = "org.eclipse.jdt.ui.javaTypeProposalCategory"; //$NON-NLS-1$

    public static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory"; //$NON-NLS-1$
    public static final String RECOMMENDERS_ALL_CATEGORY_ID = "org.eclipse.recommenders.completion.rcp.proposalCategory.intelligent"; //$NON-NLS-1$
    public static final String COMPLETION_PREFERENCE_PAGE_ID = "org.eclipse.recommenders.completion.rcp.preferencePages.completions"; //$NON-NLS-1$

    public static final String PREF_SESSIONPROCESSORS = "sessionprocessors"; //$NON-NLS-1$
}
