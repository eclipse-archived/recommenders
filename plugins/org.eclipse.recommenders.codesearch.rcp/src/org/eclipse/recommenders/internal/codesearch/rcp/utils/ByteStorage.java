/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
/**
 * 
 */
package org.eclipse.recommenders.internal.codesearch.rcp.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class ByteStorage implements IStorage, IStorageEditorInput {
    private final String data;
    private final String name;

    public ByteStorage(final String data, final String name) {
        super();
        this.data = data;
        this.name = name;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(data.getBytes());
    }

    @Override
    public IPath getFullPath() {
        return new Path(name);
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public IStorage getStorage() {
        return this;
    }

    @Override
    public String getToolTipText() {
        return "Code Example Recommendation: " + getName();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ByteStorage) {
            final ByteStorage other = (ByteStorage) obj;
            return getName().equals(other.getName());
        }
        return super.equals(obj);
    }
}
