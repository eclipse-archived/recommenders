/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.rcp.providers.helper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;

public class JavaSelectionListenerSpy {

    private final List<JavaSelectionEvent> events = newArrayList();

    public List<JavaSelectionEvent> get() {
        return events;
    }

    public void recordEvent(final JavaSelectionEvent s) {
        events.add(s);
    }

    public void verifyContains(final JavaSelectionEvent selection) {
        assertTrue(events.contains(selection));
    }

    public void verifyNotContains(final JavaSelectionEvent selection) {
        assertFalse(events.contains(selection));
    }

    public void verifyContains(final JavaSelectionEvent wanted, final int expectedNum) {
        int actualNum = 0;
        for (final JavaSelectionEvent s : events) {
            if (s.equals(wanted)) {
                actualNum++;
            }
        }
        assertEquals(actualNum, expectedNum);
    }
}
