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
package org.eclipse.recommenders.internal.analysis.rcp;

import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;

import java.io.File;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.recommenders.internal.analysis.analyzers.IDependencyFingerprintComputer;
import org.eclipse.recommenders.utils.Fingerprints;

import com.google.common.collect.Maps;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.classLoader.ShrikeClass;

public class JdtBinaryTypeEntryFingerprintComputer implements IDependencyFingerprintComputer {

    Map<File, String/* fingerprint */> fingerprints = Maps.newHashMap();

    @Override
    public String computeContainerFingerprint(final IClass clazz) {
        if (clazz.isArrayClass()) {
            // clazz = ((ArrayClass) clazz).getElementClass();
            // if(clazz == null)
            return null; // We do not learn for arrays, so we do not need the
                         // fingerprint
        }

        final ShrikeClass shrikeClazz = ensureIsInstanceOf(clazz, ShrikeClass.class);

        final ModuleEntry entry = shrikeClazz.getModuleEntry();
        if (!(entry instanceof JDTBinaryTypeEntry)) {
            return null;
        }

        return findOrCreateFingerprint((JDTBinaryTypeEntry) entry);
    }

    Lock lock = new ReentrantLock();

    private synchronized String findOrCreateFingerprint(final JDTBinaryTypeEntry entry) {
        lock.lock();
        final File f = entry.getJarFile();
        String fingerprint = fingerprints.get(f);
        if (fingerprint == null) {
            fingerprint = Fingerprints.sha1(f);
            fingerprints.put(f, fingerprint);
            System.out.println("computed fingerprint for " + f);
        }
        lock.unlock();
        return fingerprint;
    }

}
