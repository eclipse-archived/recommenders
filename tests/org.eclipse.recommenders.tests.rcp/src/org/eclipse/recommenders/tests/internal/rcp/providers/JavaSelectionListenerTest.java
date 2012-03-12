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
package org.eclipse.recommenders.tests.internal.rcp.providers;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.jface.viewers.StructuredSelection.EMPTY;
import static org.eclipse.recommenders.tests.jdt.JdtMockUtils.someField;
import static org.eclipse.recommenders.tests.jdt.JdtMockUtils.someJavaModel;
import static org.eclipse.recommenders.tests.jdt.JdtMockUtils.someLocalVariable;
import static org.eclipse.recommenders.tests.jdt.JdtMockUtils.someMethod;
import static org.eclipse.recommenders.tests.jdt.JdtMockUtils.someType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionProvider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class JavaSelectionListenerTest {

	List<IJavaElement> elements = Collections
			.synchronizedList(new ArrayList<IJavaElement>());

	JavaSelectionProvider sut = new JavaSelectionProvider(new EventBus() {
		@Override
		public void post(final Object event) {
			elements.add(((JavaSelectionEvent) event).getElement());
		};
	});

	@Test
	public void testStructuredSelectionWithType() throws InterruptedException {
		final List<?> expected = newArrayList(someType(), someMethod(),
				someField(), someLocalVariable(), someJavaModel());
		for (final Object e : expected) {
			sut.selectionChanged(null, new StructuredSelection(e));
			Thread.sleep(150);
		}
		assertEquals(expected, elements);
	}

	@Test
	public void testFireEventTwiceSelection() throws InterruptedException {

		final IType someType = someType();
		sut.selectionChanged(null, new StructuredSelection(someType));
		sut.selectionChanged(null, new StructuredSelection(someType));
		Thread.sleep(150);

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
