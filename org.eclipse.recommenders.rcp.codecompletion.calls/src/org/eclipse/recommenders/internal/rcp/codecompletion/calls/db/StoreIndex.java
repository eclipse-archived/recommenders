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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class StoreIndex {

    private final HashMap<ManifestId, ModelArchive> index = Maps.newHashMap();
    private final Multimap<ITypeName, ModelArchive> typeIndex = HashMultimap.create();
    private final Multimap<String, ITypeName> simpleTypeIndex = HashMultimap.create();

    public StoreIndex() {

    }

    public StoreIndex(final StoreIndex oldIndex) {
        index.putAll(oldIndex.index);
        typeIndex.putAll(oldIndex.typeIndex);
        simpleTypeIndex.putAll(oldIndex.simpleTypeIndex);
    }

    public boolean willAccept(final ModelArchive archive) {
        final ManifestId id = new ManifestId(archive.getManifest());
        if (index.containsKey(id)) {
            final ModelArchive prevArchive = index.get(id);
            if (prevArchive.getManifest().getTimestamp().after(archive.getManifest().getTimestamp())) {
                return false;
            }
        }
        return true;
    }

    private void addToIndex(final ModelArchive modelArchive) {
        final Manifest manifest = modelArchive.getManifest();
        final ManifestId id = new ManifestId(manifest);
        addToIndex(id, modelArchive);
    }

    private void addToIndex(final ManifestId id, final ModelArchive modelArchive) {
        if (!willAccept(modelArchive)) {
            return;
        }

        if (index.containsKey(id)) {
            removeTypesFromIndex(modelArchive);
            // TODO: Need to delete old archive
        }
        index.put(id, modelArchive);
        addTypesToIndex(modelArchive);
    }

    private void removeTypesFromIndex(final ModelArchive modelArchive) {
        final List<ITypeName> types = modelArchive.getTypes();
        for (final ITypeName type : types) {
            typeIndex.remove(type, modelArchive);
            if (!typeIndex.containsKey(type)) {
                simpleTypeIndex.remove(type.getClassName(), type);
            }
        }
    }

    private void addTypesToIndex(final ModelArchive modelArchive) {
        final List<ITypeName> types = modelArchive.getTypes();
        for (final ITypeName type : types) {
            typeIndex.put(type, modelArchive);
            simpleTypeIndex.put(type.getClassName(), type);
        }
    }

    public void addToIndex(final List<ModelArchive> archives) {
        for (final ModelArchive modelArchive : archives) {
            addToIndex(modelArchive);
        }
    }

    public boolean hasModel(final ITypeName name) {
        return typeIndex.containsKey(name);
    }

    public IObjectMethodCallsNet getModel(final ITypeName name) {
        return typeIndex.get(name).iterator().next().load(name);
    }

    public Set<IObjectMethodCallsNet> getModelsForSimpleName(final ITypeName simpleName) {
        final Collection<ITypeName> types = simpleTypeIndex.get(simpleName.getClassName());
        final HashSet<IObjectMethodCallsNet> result = new HashSet<IObjectMethodCallsNet>();
        for (final ITypeName type : types) {
            for (final ModelArchive archive : typeIndex.get(type)) {
                result.add(archive.load(type));
            }
        }
        return result;
    }

    public Manifest getManifest(final String name, final Version version) {
        return index.get(new ManifestId(name, version)).getManifest();
    }

    public List<Manifest> getAllManifests() {
        final LinkedList<Manifest> result = new LinkedList<Manifest>();
        final Collection<ModelArchive> archives = index.values();
        for (final ModelArchive modelArchive : archives) {
            result.add(modelArchive.getManifest());
        }
        return result;
    }

    private static class ManifestId {
        private final String name;
        private final Version version;

        public ManifestId(final Manifest manifest) {
            this.name = manifest.getName();
            this.version = manifest.getVersion();
        }

        public ManifestId(final String name, final Version version) {
            this.name = name;
            this.version = version;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ManifestId) {
                final ManifestId otherId = (ManifestId) obj;
                return name.equals(otherId.name) && version.equals(otherId.version);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return name.hashCode() ^ version.hashCode();
        }
    }

}
