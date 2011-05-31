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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.db;

import java.io.File;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ICallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DbCallsModelStore implements ICallsModelStore {

    @Inject
    @Named("calls.store.location")
    private File storeLocation;

    private void initialize() {
        final File[] files = storeLocation.listFiles();
        for (final File file : files) {
            if (file.getName().endsWith(".zip")) {
                initialize(new ModelArchive(file));
            }
        }
    }

    private void initialize(final ModelArchive modelArchive) {
        final Manifest manifest = modelArchive.getManifest();
    }

    @Override
    public boolean hasModel(final ITypeName name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IObjectMethodCallsNet getModel(final ITypeName name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<IObjectMethodCallsNet> getModelsForSimpleName(final ITypeName simpleName) {
        // TODO Auto-generated method stub
        return null;
    }

    private static class ManifestId {
        private Manifest manifest;

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Manifest) {
                final Manifest otherManifest = (Manifest) obj;
                return manifest.getName().equals(otherManifest.getName())
                        && manifest.getVersion().equals(otherManifest.getVersion());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return manifest.getName().hashCode() ^ manifest.getVersion().hashCode();
        }
    }
}
