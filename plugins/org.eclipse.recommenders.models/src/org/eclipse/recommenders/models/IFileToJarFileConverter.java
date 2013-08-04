/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.*;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import com.google.common.base.Optional;

public interface IFileToJarFileConverter {
    Optional<JarFile> createJarFile(File file);
}

class DefaultJarFileConverter implements IFileToJarFileConverter {

    @Override
    public Optional<JarFile> createJarFile(File file) {
        try {
            return of(new JarFile(file));
        } catch (IOException e) {
            return absent();
        }
    }
}
