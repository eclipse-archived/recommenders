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
package org.eclipse.recommenders.internal.models.rcp;

public final class Constants {

    private Constants() {
        // Not meant to be instantiated
    }

    public static final String BUNDLE_ID = "org.eclipse.recommenders.models.rcp"; //$NON-NLS-1$

    public static final String PREF_REPOSITORY_URL_LIST = "repository.url.list"; //$NON-NLS-1$
    public static final String PREF_REPOSITORY_ENABLE_AUTO_DOWNLOAD = "repository.auto.download"; //$NON-NLS-1$
    public static final String PREF_IGNORE_DOWNLOAD_FAILURES = "ignore.download.failures"; //$NON-NLS-1$
    public static final String PREF_REPOSITORY_USERNAME = "username"; //$NON-NLS-1$
    public static final String PREF_REPOSITORY_PASSWORD = "password"; //$NON-NLS-1$
}
