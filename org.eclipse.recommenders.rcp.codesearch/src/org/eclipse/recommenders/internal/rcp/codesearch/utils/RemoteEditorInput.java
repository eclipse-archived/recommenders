/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
/**
 * 
 */
package org.eclipse.recommenders.internal.rcp.codesearch.utils;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.commons.utils.IOUtils;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class RemoteEditorInput implements IStorage, IStorageEditorInput {
    private final URL data;
    private final String name;

    public RemoteEditorInput(final URL source, final String name) {
        super();
        this.data = source;
        this.name = name;
    }

    @Override
    public InputStream getContents() throws CoreException {
        InputStream stream = null;
        BufferedInputStream buffer = null;
        try {
            stream = data.openStream();
            buffer = new BufferedInputStream(stream);
            return buffer;
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(buffer);
        }
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
        if (obj instanceof RemoteEditorInput) {
            final RemoteEditorInput other = (RemoteEditorInput) obj;
            return getName().equals(other.getName());
        }
        return super.equals(obj);
    }
}
