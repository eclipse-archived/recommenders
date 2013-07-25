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
package org.eclipse.recommenders.rcp;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

import java.util.List;

public class JavaSelectionListenerSpy {

    private final List<JavaElementSelectionEvent> events = newArrayList();

    public List<JavaElementSelectionEvent> get() {
        return events;
    }

    public void recordEvent(final JavaElementSelectionEvent s) {
        events.add(s);
    }

    public void verifyContains(final JavaElementSelectionEvent selection) {
        assertTrue(events.contains(selection));
    }

    public void verifyNotContains(final JavaElementSelectionEvent selection) {
        assertFalse(events.contains(selection));
    }

    public void verifyContains(final JavaElementSelectionEvent wanted, final int expectedNum) {
        int actualNum = 0;
        for (final JavaElementSelectionEvent s : events) {
            if (s.equals(wanted)) {
                actualNum++;
            }
        }
        assertEquals(actualNum, expectedNum);
    }
}
