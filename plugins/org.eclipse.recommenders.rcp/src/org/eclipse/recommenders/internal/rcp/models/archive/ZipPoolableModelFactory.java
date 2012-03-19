/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.models.archive;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZipPoolableModelFactory<K, M> implements IModelFactory<K, M> {
    private Logger log = LoggerFactory.getLogger(getClass());
    protected ZipFile zip;

    public ZipPoolableModelFactory(File zip) throws IOException {
        this(new ZipFile(zip));
    }

    public ZipPoolableModelFactory(ZipFile zip) {
        this.zip = zip;
    }

    @Override
    public void open() {
    }

    @Override
    public boolean validateModel(K key, M model) {
        return true;
    }

    @Override
    public void passivateModel(K key, M model) {
    }

    @Override
    public void destroyModel(K key, M obj) {
    }

    @Override
    public void activateModel(K key, M model) {
    }

    @Override
    public void close() {
        try {
            zip.close();
        } catch (Exception e) {
            log.warn("Failed to close zip'" + zip + "'", e);
        }
    }
}
