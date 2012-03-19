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
package org.eclipse.recommenders.internal.completion.rcp.overrides.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.recommenders.internal.completion.rcp.overrides.net.ClassOverridesNetwork;
import org.eclipse.recommenders.internal.completion.rcp.overrides.net.ClassOverridesNetworkBuilder;
import org.eclipse.recommenders.internal.completion.rcp.overrides.net.ClassOverridesObservation;
import org.eclipse.recommenders.internal.rcp.models.archive.ZipPoolableModelFactory;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.gson.reflect.TypeToken;

public final class OverridesZipModelFactory extends ZipPoolableModelFactory<ITypeName, ClassOverridesNetwork> {
    OverridesZipModelFactory(File zip) throws IOException {
        super(zip);
    }

    @Override
    public boolean hasModel(ITypeName key) {
        return entry(key) != null;
    }

    @Override
    public ClassOverridesNetwork createModel(ITypeName key) throws IOException {
        InputStream is = zip.getInputStream(entry(key));

        final Type listType = new TypeToken<List<ClassOverridesObservation>>() {
        }.getType();
        final List<ClassOverridesObservation> observations = GsonUtil.deserialize(is, listType);
        if (observations.size() == 0) {
            // XXX sanitize bad models!
            // we still need to ensure minimum quality for models .
            observations.add(new ClassOverridesObservation());
        }
        final ClassOverridesNetworkBuilder b = new ClassOverridesNetworkBuilder(key, observations);
        b.createPatternsNode();
        b.createMethodNodes();
        final ClassOverridesNetwork network = b.build();
        return network;
    }

    private ZipEntry entry(ITypeName key) {
        String name = "class-overrides-" + key.getIdentifier().replace('/', '.') + ".json";
        return zip.getEntry(name);
    }

    @Override
    public void activateModel(ITypeName key, ClassOverridesNetwork model) {
        model.clearEvidence();
    };
}