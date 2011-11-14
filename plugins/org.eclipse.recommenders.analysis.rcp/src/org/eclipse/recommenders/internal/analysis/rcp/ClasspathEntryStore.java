/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.analysis.rcp;

import static org.eclipse.recommenders.internal.analysis.rcp.RcpAnalysisModule.CLASSPATH_ENTRY_STORE_BASEDIR;
import static org.eclipse.recommenders.utils.Checks.ensureExists;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;

import org.eclipse.recommenders.internal.analysis.analyzers.modules.ClasspathEntry;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ClasspathEntryStore {

    private final File basedir;

    @Inject
    public ClasspathEntryStore(@Named(CLASSPATH_ENTRY_STORE_BASEDIR) final File basedir) {
        this.basedir = basedir;
    }

    public boolean hasModel(final String fingerprint) {
        final File f = computeModelFileHandle(fingerprint);
        return f.exists();
    }

    public ClasspathEntry get(final String fingerprint) {
        final File f = computeModelFileHandle(fingerprint);
        ensureExists(f);
        return GsonUtil.deserialize(f, ClasspathEntry.class);
    }

    private File computeModelFileHandle(final String fingerprint) {
        return new File(basedir, fingerprint + ".json");
    }

    public void register(final ClasspathEntry entry) {
        ensureIsNotNull(entry.fingerprint);
        final File f = computeModelFileHandle(entry.fingerprint);
        GsonUtil.serialize(entry, f);
    }

    public void deregister(final ClasspathEntry entry) {
        ensureIsNotNull(entry.fingerprint);
        final File f = computeModelFileHandle(entry.fingerprint);
        f.delete();
    }
}
