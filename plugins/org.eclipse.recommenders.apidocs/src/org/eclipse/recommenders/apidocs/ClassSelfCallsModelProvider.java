/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.apidocs;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.utils.Constants.*;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.PoolingModelProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.common.base.Optional;

public class ClassSelfCallsModelProvider extends PoolingModelProvider<UniqueTypeName, ClassSelfcallDirectives> {

    public ClassSelfCallsModelProvider(IModelRepository repository, IModelIndex index) {
        super(repository, index, CLASS_SELFC_MODEL);
    }

    @Override
    protected Optional<ClassSelfcallDirectives> loadModel(ZipFile zip, UniqueTypeName key) throws Exception {
        String path = Zips.path(key.getName(), DOT_JSON);
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            return absent();
        }
        InputStream is = zip.getInputStream(entry);
        ClassSelfcallDirectives res = GsonUtil.deserialize(is, ClassSelfcallDirectives.class);
        IOUtils.closeQuietly(is);
        return of(res);
    }
}
