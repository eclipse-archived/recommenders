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
package org.eclipse.recommenders.tests.commons.mining.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;

public class Comparer {
	
	private static final double DOUBLE_DELTA = 0.001;

	public static <K,V> void assertEqualContentAndOrdering(Map<K,V> expected, Map<K,V> actual) {

		Assert.assertEquals(expected.size(), actual.size());

		Iterator<K> itE = expected.keySet().iterator();
		Iterator<K> itA = actual.keySet().iterator();

		while (itE.hasNext()) {
			K keyE = itE.next();
			K keyA = itA.next();

			// assert ordering of keys
			Assert.assertEquals(keyE, keyA);
			
			V valE = expected.get(keyE);
			V valA = actual.get(keyA);
			
			if(valE instanceof Double) {
				Double doubleE = (Double)valE;
				Double doubleA = (Double)valA;
				Assert.assertEquals(doubleE, doubleA, DOUBLE_DELTA);
			} else
				Assert.assertEquals(valE, valA);
		}
	}

	public static <T> void assertEqualContentAndOrdering(Collection<T> expected, Collection<T> actual) {

		// assert equal contents
		Assert.assertEquals(expected, actual);

		// assert ordering of keys
		Iterator<T> itE = expected.iterator();
		Iterator<T> itA = actual.iterator();

		while (itE.hasNext()) {
			T keyE = itE.next();
			T keyA = itA.next();

			Assert.assertEquals(keyE, keyA);
		}
	}
}
