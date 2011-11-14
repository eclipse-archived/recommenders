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
package org.eclipse.recommenders.utils.gson;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.junit.Test;

public class GsonDateFormatTest {

    @Test
    public void testDeserialization() {
        // setup:
        final String input = "\"2011-01-03T10:52:32.587+01:00\"";
        final Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+01:00"));
        // exercise:
        final Date date = GsonUtil.deserialize(input, Date.class);
        calendar.setTime(date);
        // verify:
        assertEquals(2011, calendar.get(Calendar.YEAR));
        assertEquals(0, calendar.get(Calendar.MONTH));
        assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(52, calendar.get(Calendar.MINUTE));
        assertEquals(32, calendar.get(Calendar.SECOND));
        assertEquals(587, calendar.get(Calendar.MILLISECOND));
        assertEquals(3600 * 1000, calendar.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void testSerialization() {
        // setup:
        final String output = "\"2011-01-03T10:52:32.587+01:00\"";
        final Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+01:00"));
        calendar.set(Calendar.YEAR, 2011);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 52);
        calendar.set(Calendar.SECOND, 32);
        calendar.set(Calendar.MILLISECOND, 587);
        // exercise:
        final String json = GsonUtil.serialize(calendar.getTime());
        // verify:
        assertEquals(output, json);
    }
}
