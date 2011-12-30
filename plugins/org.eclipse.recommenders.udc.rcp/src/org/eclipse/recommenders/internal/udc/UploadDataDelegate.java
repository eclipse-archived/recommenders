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

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.ui.IStartup;

public class UploadDataDelegate implements IStartup {
    @Override
    public void earlyStartup() {
        if (!isAutomaticUploadActive()) {
            return;
        }
        if (PreferenceUtil.needToOpenUploadWizard()) {
            return;
        }
        final AutomaticUploadJob job = new AutomaticUploadJob();
        job.setSystem(true);
        final UploadSchedule schedule = new UploadSchedule();
        job.schedule(schedule.getScheduleTime());
        addPreferenceListener(job);
    }

    private void addPreferenceListener(final Job job) {
        final IPreferenceChangeListener listener = new UploadTimeChangedListener(job);
        PreferenceUtil.getPluginNode().addPreferenceChangeListener(listener);
    }

    private boolean isAutomaticUploadActive() {
        return UploadPreferences.isUploadData();
    }

}
