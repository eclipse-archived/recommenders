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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UploadSchedule {
    public long getScheduleTime() {
        if (UploadPreferences.isUploadOnStartup()) {
            return 0;
        }
        final Date nextUploadDate = getRegularyUploadDate();
        long time = nextUploadDate.getTime() - System.currentTimeMillis();
        if (time > 0) {
            return time;
        }
        time = getScheduleTimeForToday();
        if (time > 0) {
            return time;
        }
        return getScheduleTimeForTomorrow();
    }

    private void setUploadHour(final Calendar calendar) {
        if (UploadPreferences.isUploadAtAnyHour()) {
            calendar.set(Calendar.HOUR_OF_DAY, new GregorianCalendar().get(Calendar.HOUR_OF_DAY));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, UploadPreferences.getUploadHour());
        }
    }

    private long getScheduleTimeForToday() {
        final Calendar uploadDate = new GregorianCalendar();

        setUploadHour(uploadDate);

        uploadDate.set(Calendar.MINUTE, 0);
        return uploadDate.getTimeInMillis() - System.currentTimeMillis();
    }

    private long getScheduleTimeForTomorrow() {
        final Calendar uploadDate = new GregorianCalendar();
        uploadDate.add(Calendar.DAY_OF_YEAR, 1);
        setUploadHour(uploadDate);
        uploadDate.set(Calendar.MINUTE, 0);
        return uploadDate.getTimeInMillis() - System.currentTimeMillis();
    }

    private Date getLastUploadDate() {
        final long lastUpload = UploadPreferences.getLastUploadDate();
        final Date lastUploadDate = new Date(lastUpload);
        return lastUploadDate;
    }

    private Date getRegularyUploadDate() {
        final Date lastUpload = getLastUploadDate();
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(lastUpload);

        final Calendar uploadDate = new GregorianCalendar();
        uploadDate.setTime(lastUpload);
        uploadDate.add(Calendar.DAY_OF_YEAR, UploadPreferences.getUploadIntervalDays());
        setUploadHour(calendar);
        return uploadDate.getTime();
    }
}