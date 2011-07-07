/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsInstanceOf;

import java.io.File;
import java.util.Map;

import org.eclipse.recommenders.commons.utils.Fingerprints;

import com.google.common.collect.Maps;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;

public class JarFileModuleEntryFingerprintComputer implements IDependencyFingerprintComputer {

    Map<File, String/* fingerprint */> fingerprints = Maps.newHashMap();

    @Override
    public String computeContainerFingerprint(final IClass clazz) {

        final ShrikeClass shrikeClazz = ensureIsInstanceOf(clazz, ShrikeClass.class);
        final JarFileEntry entry = ensureIsInstanceOf(shrikeClazz.getModuleEntry(), JarFileEntry.class);
        return findOrCreateFingerprint(entry);
    }

    private synchronized String findOrCreateFingerprint(final JarFileEntry jarFileEntry) {
        final File f = new File(jarFileEntry.getJarFile().getName());
        String fingerprint = fingerprints.get(f);
        if (fingerprint == null) {
            fingerprint = Fingerprints.sha1(jarFileEntry.getInputStream());
            fingerprints.put(f, fingerprint);
        }
        return fingerprint;
    }

}
