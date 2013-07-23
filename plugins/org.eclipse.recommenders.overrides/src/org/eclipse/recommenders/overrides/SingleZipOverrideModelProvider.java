/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.overrides;

import static org.eclipse.recommenders.utils.Zips.*;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.models.BasedTypeName;
import org.eclipse.recommenders.models.IBasedName;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * A model provider that uses a single zip file to resolve and load call models from.
 * <p>
 * Note that this provider does not implement any pooling behavior, i.e., calls to {@link #acquireModel(BasedTypeName)}
 * may return the <b>same</b> {@link ICallModel} independent of whether {@link #releaseModel(ICallModel)} was called or
 * not. Thus, these <b>models should not be shared between and used by several recommenders at the same time</b>.
 */
public class SingleZipOverrideModelProvider implements IOverrideModelProvider, Openable {

    private final File models;
    private ZipFile zip;

    public SingleZipOverrideModelProvider(File models) {
        this.models = models;
    }

    @Override
    public void open() throws IOException {
        readFully(models);
        zip = new ZipFile(models);
    }

    @Override
    public void close() throws IOException {
        closeQuietly(zip);
    }

    public Set<ITypeName> content() {
        TreeSet<ITypeName> content = Sets.newTreeSet();
        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
            ZipEntry next = e.nextElement();
            if (next.isDirectory() || next.getName().startsWith("META-INF")) {
                continue;
            }
            ITypeName type = Zips.type(next, ".json");
            content.add(type);
        }
        return content;
    }

    @Override
    public void releaseModel(IOverrideModel value) {
    }

    @Override
    public Optional<IOverrideModel> acquireModel(IBasedName<ITypeName> key) {
        try {
            return JayesOverrideModel.load(zip, key.getName());
        } catch (IOException e) {
            return Optional.absent();
        }
    }
}
