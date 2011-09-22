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

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

public class UpdateCompletedListener implements IJobChangeListener {

    @Override
    public void aboutToRun(final IJobChangeEvent event) {

    }

    @Override
    public void awake(final IJobChangeEvent event) {

    }

    @Override
    public void done(final IJobChangeEvent event) {
        if (event.getResult().isOK()) {
            UpdatePreferences.setLastUpdateDate(System.currentTimeMillis());
        }
    }

    @Override
    public void running(final IJobChangeEvent event) {

    }

    @Override
    public void scheduled(final IJobChangeEvent event) {

    }

    @Override
    public void sleeping(final IJobChangeEvent event) {

    }

}
