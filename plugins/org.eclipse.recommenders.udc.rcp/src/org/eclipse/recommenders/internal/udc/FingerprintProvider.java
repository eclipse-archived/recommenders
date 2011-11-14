/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.calls.rcp.store.ClasspathDependencyStore;

import com.google.common.collect.Sets;

public class FingerprintProvider {

    ClasspathDependencyStore classpathDependencyStore;

    @Inject
    public FingerprintProvider(final ClasspathDependencyStore classpathDependencyStore) {
        super();
        this.classpathDependencyStore = classpathDependencyStore;
    }

    public Set<String> getFingerprints(final String[] libraries) {
        final Set<String> result = Sets.newHashSet();
        for (final String absolutPath : libraries) {
            final File library = new File(absolutPath);
            if (classpathDependencyStore.containsClasspathDependencyInfo(library)) {
                final String fingerPrint = classpathDependencyStore.getClasspathDependencyInfo(library).jarFileFingerprint;
                result.add(fingerPrint);
            }
        }
        return result;
    }

    public static FingerprintProvider createInstance() {
        return InjectionService.getInstance().requestInstance(FingerprintProvider.class);
    }

}
