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
package org.eclipse.recommenders.internal.rcp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.rcp.messages"; //$NON-NLS-1$
    public static String JOB_INITIALIZE_PROJECTS;
    public static String JOB_UPDATE_MODEL_INDEX;
    public static String JOB_CLEAR_MODEL_REPOSITORY;
    public static String JOB_RESOLVING_MODEL;
    public static String TASK_RESOLVING;
    public static String TASK_LOOKING_FOR_MODEL;
    public static String TASK_DOWNLOADING;
    public static String TASK_UPLOADING;
    public static String STATUS_DOWNLOAD_FINISHED;
    public static String STATUS_DOWNLOAD_CORRUPTED;
    public static String STATUS_DOWNLOAD_FAILED;
    public static String PREFPAGE_OVERVIEW_INTRO;
    public static String PREFPAGE_CLEAR_CACHES;
    public static String PREFPAGE_ENABLE_AUTO_DOWNLOAD;
    public static String PREFPAGE_MODEL_REPOSITORY_HEADLINE;
    public static String PREFPAGE_MODEL_REPOSITORY_INTRO;
    public static String PREFPAGE_URI;
    public static String PREFPAGE_URI_INSERT;
    public static String PREFPAGE_URI_INVALID;
    public static String PREFPAGE_URI_MODEL_REPOSITORY;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
