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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

/**
 * Performs a pre-fetching of all available types from store...
 */
public class ModelStoreInitializerJob extends WorkspaceJob {

    public ModelStoreInitializerJob() {
        super("Initalizing Calls Re" + "commender...");
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Fetching available models from store...", 1);
        try {
            prefetch();
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    private void prefetch() {
        final CallsModelStore s = InjectionService.getInstance().requestInstance(CallsModelStore.class);
        s.hasModel(VmTypeName.BOOLEAN);
    }
}
