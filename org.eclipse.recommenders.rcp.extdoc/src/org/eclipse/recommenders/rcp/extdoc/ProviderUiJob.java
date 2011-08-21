/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * ProviderUiJobs are created by the providers on a selection update as
 * definition of composite updates carried out, i.e. what content should be
 * displayed.
 */
public abstract class ProviderUiJob {

    /**
     * @param job
     *            The UI update job created by the provider for the last
     *            selection.
     * @param composite
     *            The composite in which the UI job will fill its content.
     */
    public static void run(final ProviderUiJob job, final Composite composite) {
        final UIJob uiJob = new UIJob("Updating Provider View") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!composite.isDisposed()) {
                    try {
                        job.run(composite);
                    } catch (final Exception e) {
                        ExtDocPlugin.logException(e);
                    }
                    composite.getParent().layout(true, true);
                    composite.getParent().getParent().layout(true);
                }
                return Status.OK_STATUS;
            }
        };
        uiJob.schedule();
    }

    /**
     * @param composite
     *            The composite which shall be filled with the provider's
     *            content.
     */
    public abstract void run(Composite composite);

}
