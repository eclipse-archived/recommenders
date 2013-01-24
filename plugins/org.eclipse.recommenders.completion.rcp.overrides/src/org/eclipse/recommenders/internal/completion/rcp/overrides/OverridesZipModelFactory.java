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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.rcp.models.archive.ZipPoolableModelFactory;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.gson.reflect.TypeToken;

public final class OverridesZipModelFactory extends ZipPoolableModelFactory<IType, ClassOverridesNetwork> {
    private JavaElementResolver jdtResolver;

    public OverridesZipModelFactory(File zip, JavaElementResolver jdtResolver) throws IOException {
        super(zip);
        this.jdtResolver = jdtResolver;
    }

    @Override
    public boolean hasModel(IType key) {
        return getEntry(key) != null;
    }

    private ZipEntry getEntry(IType jType) {
        ITypeName rType = toRecName(jType);
        String name = rType.getIdentifier().substring(1) + ".json";
        return zip.getEntry(name);
    }

    private ITypeName toRecName(IType jType) {
        ITypeName rType = jdtResolver.toRecType(jType);
        return rType;
    }

    @Override
    public ClassOverridesNetwork createModel(IType key) throws IOException {
        InputStream is = null;
        try {
            ITypeName typeName = toRecName(key);
            is = zip.getInputStream(getEntry(key));

            final Type listType = new TypeToken<List<ClassOverridesObservation>>() {
            }.getType();
            final List<ClassOverridesObservation> observations = GsonUtil.deserialize(is, listType);
            if (observations.size() == 0) {
                // XXX sanitize bad models!
                // we still need to ensure minimum quality for models .
                observations.add(new ClassOverridesObservation());
            }
            final ClassOverridesNetworkBuilder b = new ClassOverridesNetworkBuilder(typeName, observations);
            b.createPatternsNode();
            b.createMethodNodes();
            final ClassOverridesNetwork network = b.build();
            return network;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public void activateModel(IType key, ClassOverridesNetwork model) {
        model.clearEvidence();
    };
}
