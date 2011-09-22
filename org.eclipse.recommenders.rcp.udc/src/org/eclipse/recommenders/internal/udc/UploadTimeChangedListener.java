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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

public class UploadTimeChangedListener implements IPreferenceChangeListener {
    Job job;

    public UploadTimeChangedListener(final Job job) {
        this.job = job;
    }

    @Override
    public void preferenceChange(final PreferenceChangeEvent event) {
        final String key = event.getKey();
        if (key.equals(PreferenceKeys.uploadInvervalDay) || key.equals(PreferenceKeys.uploadInvervalHour)
                || key.equals(PreferenceKeys.uploadIntervalAnyHour) || key.equals(PreferenceKeys.uploadData)) {
            cancleScheduling();
        }
    }

    private void cancleScheduling() {
        job.cancel();
    }

}
