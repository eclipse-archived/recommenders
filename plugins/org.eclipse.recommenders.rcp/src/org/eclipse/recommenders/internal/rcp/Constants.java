/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn, Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static final String PREF_UUID = "recommenders.uuid"; //$NON-NLS-1$
    public static final String BUNDLE_ID = "org.eclipse.recommenders.rcp"; //$NON-NLS-1$

    public static final String PREF_IGNORE_BUNDLE_RESOLUTION_FAILURE = "ignore.bundle.resolution.failures"; //$NON-NLS-1$
    public static final String NEWS_ENABLED = "news-enabled"; //$NON-NLS-1$
    public static final String NEWS_LAST_CHECK = "news-last-check"; //$NON-NLS-1$
}
