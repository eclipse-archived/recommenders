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
package org.eclipse.recommenders.tests.commons.mining.dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.recommenders.commons.mining.dictionary.Dictionary;
import org.eclipse.recommenders.commons.mining.dictionary.IMatcher;
import org.eclipse.recommenders.tests.commons.mining.utils.Comparer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DictionaryTest {

	private Dictionary<String> uut;

	@Before
	public void setup() {
		uut = new Dictionary<String>();
	}

	@Test
	public void defaultValues() {
		uut = new Dictionary<String>();

		Assert.assertEquals(0, uut.size());
		Assert.assertEquals(0, uut.getAllEntries().size());
	}

	@Test
	public void correctSize() {

		Assert.assertEquals(0, uut.size());

		uut.add("bla");
		uut.add("blubb");

		Assert.assertEquals(2, uut.size());
	}

	@Test
	public void valuesCanBeCleared() {
		uut.add("asd");
		uut.clear();

		Set<String> actual = uut.getAllEntries();
		Set<String> expected = Sets.newHashSet();

		assertEquals(expected, actual);
	}

	@Test
	public void valuesCanBeRemoved() {
		uut.add("a");
		uut.add("b");
		uut.add("c");

		uut.remove("b");
		uut.remove("c");

		Set<String> actual = uut.getAllEntries();
		Set<String> expected = Sets.newLinkedHashSet();
		expected.add("a");

		assertEquals(expected, actual);
	}

	@Test
	public void nonExistingValuesHaveNoId() {
		uut.add("a");
		uut.remove("a");
		int actual = uut.getId("a");
		int expected = -1;
		assertEquals(expected, actual);
	}

	@Test
	public void valuesAreNotRecordedMultipleTimes() {
		uut.add("a");
		uut.add("a");
		Set<String> actual = uut.getAllEntries();
		Set<String> expected = Sets.newLinkedHashSet();
		expected.add("a");
		assertEquals(expected, actual);
	}

	@Test
	public void multipleAddsReturnEqualId() {
		int expected = uut.add("a");
		int actual = uut.add("a");
		assertEquals(expected, actual);
	}

	@Test
	public void correctLinkBetweenKeyAndValue() {

		int first = uut.add("first");
		int second = uut.add("second");

		Assert.assertEquals(0, first);
		Assert.assertEquals(1, second);

		Assert.assertEquals(first, uut.getId("first"));
		Assert.assertEquals(second, uut.getId("second"));

		Assert.assertEquals("first", uut.getEntry(0));
		Assert.assertEquals("second", uut.getEntry(1));
	}

	@Test
	public void allEntriesAreGivenBack() {

		uut.add("a");
		uut.add("b");
		uut.add("c");

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("a");
		expected.add("b");
		expected.add("c");

		Assert.assertEquals(expected, uut.getAllEntries());
	}

	@Test
	public void matching() {

		uut.add("Aa");
		uut.add("Ab");
		uut.add("Bc");

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Aa");
		expected.add("Ab");

		IMatcher<String> m = new IMatcher<String>() {
			@Override
			public boolean matches(String entry) {
				return entry.startsWith("A");
			}
		};

		Assert.assertEquals(expected, uut.getAllMatchings(m));
	}

	@Test
	public void containingEntries() {

		uut.add("a");

		Assert.assertTrue(uut.contains("a"));
		Assert.assertFalse(uut.contains("nonExistant"));
	}

	@Test
	public void massInsertionKeepsOrdering() {
		uut = createRandomDictionary();

		int i = 0;
		for (String actual : uut.getAllEntries()) {

			String expected = "entry" + i++;

			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void serializationKeepsOrdering() {

		uut = createRandomDictionary();

		Gson gson = new Gson();
		Type dictType = new TypeToken<Dictionary<String>>() {
		}.getType();

		String json = gson.toJson(uut, dictType);
		Dictionary<String> dict = gson.fromJson(json, dictType);

		assertEqual(uut, dict);
	}

	@Test
	public void toStringConcatenatesContent() {
		uut.add("bla");
		uut.add("blubb");
		uut.add("abc");

		String actual = uut.toString();
		String expected = "[bla,\nblubb,\nabc,\n]";

		assertEquals(expected, actual);
	}

	@Test
	public void equalObjectsAreDetected() {
		Dictionary<String> a = new Dictionary<String>();
		a.add("a");
		a.add("b");
		Dictionary<String> b = new Dictionary<String>();
		b.add("a");
		b.add("b");

		assertEquals(a, b);
		assertTrue(a.hashCode() == b.hashCode());
	}

	@Test
	public void differentObjectsAreDetected() {
		Dictionary<String> a = new Dictionary<String>();
		a.add("a");
		a.add("b");
		Dictionary<String> b = new Dictionary<String>();
		b.add("a");
		b.add("x");

		assertFalse(a.equals(b));
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals("otherType"));
	}
	
	@Test
	public void removingShiftsAllIds() {
		Dictionary<String> d = new Dictionary<String>();
		d.add("a");
		d.add("b");
		
		assertEquals(1, d.getId("b"));
		d.remove("a");
		assertEquals(0, d.getId("b"));
	}

	public static Dictionary<String> createRandomDictionary() {

		Dictionary<String> dictionary = new Dictionary<String>();

		for (int i = 0; i < 200; i++) {
			String entry = "entry" + i;
			dictionary.add(entry);
		}

		return dictionary;
	}

	public static <T> void assertEqual(Dictionary<T> a, Dictionary<T> b) {
		Comparer.assertEqualContentAndOrdering(a.getAllEntries(), b.getAllEntries());
	}

}
