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
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.BayesNetWrapper;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.models.archive.ZipPoolableModelFactory;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.common.io.Closeables;

public class CallNetZipModelFactory extends ZipPoolableModelFactory<IType, IObjectMethodCallsNet> {

    private final JavaElementResolver jdtResolver;

    public CallNetZipModelFactory(File zip, JavaElementResolver jdtResolver) throws IOException {
        super(zip);
        this.jdtResolver = jdtResolver;
    }

    @Override
    public boolean hasModel(IType key) {
        return getEntry(key) != null;
    }

    private ZipEntry getEntry(IType jType) {
        ITypeName rType = toRecName(jType);
        String name = rType.getIdentifier().substring(1) + ".data";
        return zip.getEntry(name);
    }

    private ITypeName toRecName(IType jType) {
        ITypeName rType = jdtResolver.toRecType(jType);
        return rType;
    }

    @Override
    public IObjectMethodCallsNet createModel(IType key) throws Exception {

        InputStream is = null;
        ObjectInputStream ois = null;
        try {
            is = zip.getInputStream(getEntry(key));
            ois = new ObjectInputStream(is);
            BayesianNetwork net = (BayesianNetwork) ois.readObject();
            ITypeName rKey = toRecName(key);
            return new BayesNetWrapper(rKey, net);
        } finally {
            Closeables.closeQuietly(is);
            Closeables.closeQuietly(ois);
        }
    }
}
