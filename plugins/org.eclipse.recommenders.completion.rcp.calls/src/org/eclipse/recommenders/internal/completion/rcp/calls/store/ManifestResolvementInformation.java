/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store;

import java.util.Date;

import org.eclipse.recommenders.commons.udc.Manifest;

public class ManifestResolvementInformation {

    private Manifest manifest;
    private boolean manualResolved;
    private Date timestamp;

    protected ManifestResolvementInformation() {
        // for deserialization
    }

    public ManifestResolvementInformation(final Manifest manifest, final boolean manualResolved) {
        this.manifest = manifest;
        this.manualResolved = manualResolved;
        timestamp = new Date();
    }

    public Manifest getManifest() {
        return manifest;
    }

    public boolean isResolvedManual() {
        return manualResolved;
    }

    public Date getResolvingTimestamp() {
        return timestamp;
    }
}
