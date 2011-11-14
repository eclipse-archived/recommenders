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
package org.eclipse.recommenders.internal.analysis.rcp.cp;

import java.io.File;

import org.eclipse.core.resources.IFile;

import com.ibm.wala.classLoader.SourceFileModule;

/**
 * A module which is a wrapper around a .java file
 */
public class EclipseSourceFileModule extends SourceFileModule {
    private final IFile f;

    public static EclipseSourceFileModule createEclipseSourceFileModule(final IFile f) {
        if (f == null) {
            throw new IllegalArgumentException("null f");
        }
        return new EclipseSourceFileModule(f);
    }

    private EclipseSourceFileModule(final IFile f) {
        super(new File(f.getLocation().toOSString()), f.getLocation().lastSegment());
        this.f = f;
    }

    public IFile getIFile() {
        return f;
    }

    @Override
    public String toString() {
        return "EclipseSourceFileModule:" + getFile().toString();
    }
}
