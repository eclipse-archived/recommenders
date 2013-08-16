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
package org.eclipse.recommenders.models;

public class DownloadCallback {

    public static final DownloadCallback NULL = new DownloadCallback();

    public void downloadSucceeded() {
    }

    public void downloadCorrupted() {
    }

    public void downloadFailed() {
    }

    public void downloadInitiated() {
    }

    public void downloadProgressed(long transferredBytes, long totalBytes) {
    }

    public void downloadStarted() {
    }
}
