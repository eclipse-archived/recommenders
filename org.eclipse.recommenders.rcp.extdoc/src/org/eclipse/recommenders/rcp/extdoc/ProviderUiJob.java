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

public abstract class ProviderUiJob {

    public static void run(final ProviderUiJob job) {
        new UIJob("Updating Provider View") {

            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                final Composite composite = job.run();
                if (!composite.isDisposed()) {
                    composite.getParent().getParent().layout(true, true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public abstract Composite run();

}
