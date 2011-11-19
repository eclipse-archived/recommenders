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
package org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection;

public class JavaSelectionListenerSpy {

    private List<JavaSelection> events = newArrayList();

    public List<JavaSelection> get() {
        return events;
    }

    public void recordEvent(final JavaSelection s) {
        events.add(s);
    }

    public void verifyContains(JavaSelection selection) {
        assertTrue(events.contains(selection));
    }

    public void verifyNotContains(JavaSelection selection) {
        assertFalse(events.contains(selection));
    }

    public void verifyContains(JavaSelection wanted, int expectedNum) {
        int actualNum = 0;
        for (JavaSelection s : events) {
            if (s.equals(wanted)) {
                actualNum++;
            }
        }
        assertEquals(actualNum, expectedNum);
    }
}