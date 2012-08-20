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
package org.eclipse.recommenders.internal.completion.rcp.calls.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.BayesNetWrapper;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.models.archive.ZipPoolableModelFactory;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

public class CallNetZipModelFactory extends ZipPoolableModelFactory<IType, IObjectMethodCallsNet> {

    // 64mb object output stream
    private static final long MAX_MODEL_SIZE = 64 * 1024 * 1024;

    private final JavaElementResolver jdtResolver;
    private Logger log = LoggerFactory.getLogger(getClass());

    public CallNetZipModelFactory(File zip, JavaElementResolver jdtResolver) throws IOException {
        super(zip);
        this.jdtResolver = jdtResolver;
    }

    public CallNetZipModelFactory(ZipFile zip, JavaElementResolver jdtResolver) throws IOException {
        super(zip);
        this.jdtResolver = jdtResolver;
    }

    @Override
    public void destroyModel(IType key, IObjectMethodCallsNet obj) {
        super.destroyModel(key, obj);
        log.debug("Destroying model for '{}'", key.getElementName());
    }

    @Override
    public boolean hasModel(IType key) {
        return getEntry(key) != null;
    }

    private ZipEntry getEntry(IType jType) {
        ITypeName rType = toRecName(jType);

        String name = Zips.path(rType, ".data");
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) {
            return null;
        }
        if (entry.getSize() > MAX_MODEL_SIZE) {
            return null;
        }
        return entry;
    }

    private ITypeName toRecName(IType jType) {
        ITypeName rType = jdtResolver.toRecType(jType);
        return rType;
    }

    @Override
    public IObjectMethodCallsNet createModel(IType key) throws Exception {
        log.debug("Loading model for '{}'", key.getElementName());
        InputStream is = null;
        try {
            is = zip.getInputStream(getEntry(key));
            BayesianNetwork net = BayesianNetwork.read(is);
            ITypeName rKey = toRecName(key);
            return new BayesNetWrapper(rKey, net);
        } finally {
            Closeables.closeQuietly(is);
        }
    }
}
