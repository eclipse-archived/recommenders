/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MessageUtils {

    public enum MessageAge {
        TODAY(0),
        YESTERDAY(1),
        THIS_WEEK(2),
        LAST_WEEK(3),
        THIS_MONTH(4),
        LAST_MONTH(5),
        THIS_YEAR(6),
        OLDER(7),
        UNDETERMINED(8);

        private final int index;

        MessageAge(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static Date getPeriodStartDate(MessageAge messageAge, Date today, Locale locale) {
        Calendar calendar = GregorianCalendar.getInstance(locale);
        calendar.setTime(today);
        if (messageAge == MessageAge.TODAY) {
            return calendar.getTime();
        } else if (messageAge == MessageAge.YESTERDAY) {
            calendar.add(Calendar.DATE, -1);
        } else if (messageAge == MessageAge.THIS_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (messageAge == MessageAge.LAST_WEEK) {
            int firstDayOfWeek = calendar.getFirstDayOfWeek();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        } else if (messageAge == MessageAge.THIS_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.LAST_MONTH) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.THIS_YEAR) {
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else if (messageAge == MessageAge.OLDER) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
    }

}
