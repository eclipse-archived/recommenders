/**
 * Copyright (c) 2010 Darmstadt University of Technology.
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
package org.eclipse.recommenders.internal.rcp.analysis;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.recommenders.internal.commons.analysis.utils.IJarFileProvider;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;

@SuppressWarnings("restriction")
public class JDTBinaryTypeEntry implements ModuleEntry, IJarFileProvider {

    private final BinaryType b;

    public JDTBinaryTypeEntry(final BinaryType b) {
        this.b = b;
    }

    @Override
    public Module asModule() {
        return null;
    }

    @Override
    public String getClassName() {
        return "L" + b.getFullyQualifiedName().replace('.', '/');
    }

    @Override
    public InputStream getInputStream() {
        try {
            final ByteArrayInputStream bytes = new ByteArrayInputStream(b.getClassFile().getBytes());
            return bytes;
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    @Override
    public String getName() {
        return getClassName();
    }

    @Override
    public boolean isClassFile() {
        return true;
    }

    @Override
    public boolean isModuleFile() {
        return false;
    }

    @Override
    public boolean isSourceFile() {
        return false;
    }

    @Override
    public File getJarFile() {
        File res = null;
        final IResource resource = b.getResource();
        if (resource != null) {
            final IPath location = resource.getLocation();
            res = location.toFile();
        } else {
            final IPath path = b.getPath();
            res = path.toFile();
        }
        return res;
    }
}
