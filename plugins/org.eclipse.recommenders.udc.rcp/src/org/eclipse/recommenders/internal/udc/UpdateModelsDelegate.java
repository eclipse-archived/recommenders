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

import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.calls.rcp.store.UpdateAllModelsJob;
import org.eclipse.ui.IStartup;

public class UpdateModelsDelegate implements IStartup {

    public static long oneDayInMs = 24 * 60 * 60 * 1000;

    @Override
    public void earlyStartup() {
        if (!UpdatePreferences.isUpdateModels()) {
            return;
        }
        if (isTimeToUpdate()) {
            startUpdateJob();
        }
    }

    private void startUpdateJob() {
        final UpdateAllModelsJob job = InjectionService.getInstance().requestInstance(UpdateAllModelsJob.class);
        job.addJobChangeListener(new UpdateCompletedListener());
        job.schedule();
    }

    private boolean isTimeToUpdate() {
        final long lastUpdate = UpdatePreferences.getLastUpdateDate();
        final long nextUpdate = lastUpdate + UpdatePreferences.getUpdateInterval() * oneDayInMs;
        return nextUpdate > System.currentTimeMillis();
    }
}
