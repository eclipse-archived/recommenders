/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.jface.viewers.StructuredSelection.EMPTY;
import static org.eclipse.recommenders.testing.jdt.JdtMockUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.eventbus.EventBus;

public class JavaElementSelectionServiceTest {

    volatile List<IJavaElement> elements = Collections.synchronizedList(new ArrayList<IJavaElement>());

    JavaElementSelectionService sut = new JavaElementSelectionService(new EventBus() {
        @Override
        public void post(final Object event) {
            elements.add(((JavaElementSelectionEvent) event).getElement());
        };
    }) {
        {
            d = Mockito.mock(ScheduledThreadPoolExecutor.class);
            Mockito.when(d.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(
                    new Answer<Void>() {

                        @Override
                        public Void answer(InvocationOnMock invocation) throws Throwable {
                            Runnable runnable = (Runnable) invocation.getArguments()[0];
                            runnable.run();

                            return null;
                        }
                    });
        }
    };

    @Test
    public void testStructuredSelectionWithType() throws InterruptedException {
        final List<?> expected = newArrayList(someType(), someMethod(), someField(), someLocalVariable(),
                someJavaModel());
        for (final Object e : expected) {
            sut.selectionChanged(null, new StructuredSelection(e));
        }
        assertEquals(expected, elements);
    }

    @Test
    public void testFireEventTwiceSelection() throws InterruptedException {

        final IType someType = someType();
        sut.selectionChanged(null, new StructuredSelection(someType));
        sut.selectionChanged(null, new StructuredSelection(someType));

        assertEquals(1, elements.size());
    }

    @Test
    public void testEmptyStructuredSelection() {
        sut.selectionChanged(null, EMPTY);
        assertTrue(elements.isEmpty());
    }

    @Test
    public void testAnyUnknownSelectionType() {
        sut.selectionChanged(null, mock(ISelection.class));
        assertTrue(elements.isEmpty());
    }

}
