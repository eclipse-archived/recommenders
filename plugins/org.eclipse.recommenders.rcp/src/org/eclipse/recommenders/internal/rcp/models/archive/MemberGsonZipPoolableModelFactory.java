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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.gson.reflect.TypeToken;

public class MemberGsonZipPoolableModelFactory<T> extends ZipPoolableModelFactory<IMember, T> {

    private final Type type;
    private JavaElementResolver jdtResolver;

    public MemberGsonZipPoolableModelFactory(ZipFile file, TypeToken<T> type, JavaElementResolver jdtResolver)
            throws IOException {
        super(file);
        this.jdtResolver = jdtResolver;
        this.type = type.getType();
    }

    public MemberGsonZipPoolableModelFactory(File file, TypeToken<T> type, JavaElementResolver jdtResolver)
            throws IOException {
        this(new ZipFile(file), type, jdtResolver);
    }

    public MemberGsonZipPoolableModelFactory(ZipFile file, Class<T> type, JavaElementResolver jdtResolver)
            throws IOException {
        super(file);
        this.type = type;
        this.jdtResolver = jdtResolver;
    }

    public MemberGsonZipPoolableModelFactory(File file, Class<T> type, JavaElementResolver jdtResolver)
            throws IOException {
        this(new ZipFile(file), type, jdtResolver);
    }

    @Override
    public boolean hasModel(IMember key) {
        ZipEntry entry = getEntry(key);
        return entry != null;
    }

    private ZipEntry getEntry(IMember m) {
        String name = null;
        switch (m.getElementType()) {
        case IJavaElement.TYPE:
            ITypeName rType = jdtResolver.toRecType((IType) m);
            name = Zips.path(rType, ".json");
            break;
        case IJavaElement.METHOD:
            Optional<IMethodName> opt = jdtResolver.toRecMethod((IMethod) m);
            if (opt.isPresent())
                name = Zips.path(opt.get(), ".json");
            break;
        }
        return zip.getEntry(name);
    }

    @Override
    public T createModel(IMember key) throws Exception {
        InputStream is = null;
        try {
            ZipEntry entry = getEntry(key);
            is = zip.getInputStream(entry);
            String data = new String(ByteStreams.toByteArray(is));
            return GsonUtil.deserialize(data, type);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
