/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import javax.inject.Inject;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.commons.lfm.LibraryIdentifier;

public class FragmentResolver {

    private final FragmentIndex fragmentIndex;
    private final RemoteResolverJobFactory jobfactory;

    @Inject
    public FragmentResolver(final FragmentIndex fragmentIndex, final RemoteResolverJobFactory jobfactory) {
        this.fragmentIndex = fragmentIndex;
        this.jobfactory = jobfactory;
    }

    public void resolve(final IPackageFragmentRoot[] packageFragmentRoots) {
        for (final IPackageFragmentRoot packageRoot : packageFragmentRoots) {
            if (!fragmentIndex.contains(packageRoot)) {
                fragmentIndex.put(packageRoot, LibraryIdentifier.UNKNOWN);

                scheduleJob(packageRoot);
            }
        }
    }

    private void scheduleJob(final IPackageFragmentRoot packageRoot) {
        // new LocalLibraryIdentifierResolverJob(packageRoot,
        // fragmentIndex).schedule();
        final RemoteLibraryIdentifierResolverJob job = jobfactory.create(packageRoot, fragmentIndex);
        job.schedule();
    }

}
