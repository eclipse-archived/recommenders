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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ICallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CallsModelArchiveStore implements ICallsModelStore, IModelArchiveStore {

    @Inject
    @Named("calls.store.location")
    private File storeLocation;
    private StoreIndex index = new StoreIndex();
    private final ReentrantReadWriteLock indexReplacementLock = new ReentrantReadWriteLock();
    private final ReentrantLock indexUpdatingLock = new ReentrantLock();

    // TODO: Who calls this method?
    private void initializeFromStoreLocation() {
        final List<ModelArchive> archives = new LinkedList<ModelArchive>();
        final File[] files = storeLocation.listFiles();
        for (final File file : files) {
            if (file.getName().endsWith(".zip")) {
                archives.add(new ModelArchive(file));
            }
        }
        updateIndex(archives);
    }

    private void updateIndex(final List<ModelArchive> archives) {
        indexUpdatingLock.lock();
        final StoreIndex newIndex = new StoreIndex(index);
        newIndex.addToIndex(archives);
        final WriteLock writeLock = indexReplacementLock.writeLock();
        writeLock.lock();
        index = newIndex;
        writeLock.unlock();
        indexUpdatingLock.unlock();
    }

    @Override
    public void store(final ModelArchive archive) {
        indexUpdatingLock.lock();
        if (index.willAccept(archive)) {
            archive.move(new File(storeLocation, createFilename(archive)));
            updateIndex(Lists.newArrayList(archive));
        } else {
            archive.delete();
        }
        indexUpdatingLock.unlock();
    }

    private String createFilename(final ModelArchive archive) {
        final Manifest manifest = archive.getManifest();
        return manifest.getName() + "_" + manifest.getVersion() + "_" + manifest.getVersion() + ".zip";
    }

    @Override
    public boolean hasModel(final ITypeName name) {
        final ReadLock readLock = indexReplacementLock.readLock();
        readLock.lock();
        final boolean hasModel = index.hasModel(name);
        readLock.unlock();
        return hasModel;
    }

    @Override
    public IObjectMethodCallsNet getModel(final ITypeName name) {
        final ReadLock readLock = indexReplacementLock.readLock();
        readLock.lock();
        final IObjectMethodCallsNet model = index.getModel(name);
        readLock.unlock();
        return model;
    }

    @Override
    public Set<IObjectMethodCallsNet> getModelsForSimpleName(final ITypeName simpleName) {
        final ReadLock readLock = indexReplacementLock.readLock();
        readLock.lock();
        final Set<IObjectMethodCallsNet> models = index.getModelsForSimpleName(simpleName);
        readLock.unlock();
        return models;
    }

    @Override
    public Manifest getManifest(final String name, final Version version) {
        final ReadLock readLock = indexReplacementLock.readLock();
        readLock.lock();
        final Manifest manifest = index.getManifest(name, version);
        readLock.unlock();
        return manifest;
    }

    @Override
    public List<Manifest> getAllManifests() {
        final ReadLock readLock = indexReplacementLock.readLock();
        readLock.lock();
        final List<Manifest> result = index.getAllManifests();
        readLock.unlock();
        return result;
    }

}
