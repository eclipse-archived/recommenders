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
package org.eclipse.recommenders.commons.mining.dictionary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Dictionary<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient HashMap<T, Integer> entryCache = null;

	private final LinkedList<T> entries = new LinkedList<T>();

	public int add(T entry) {
		ensureCache();
		if (contains(entry)) {
			return getId(entry);
		} else {
			entries.add(entry);
			Integer id = entries.size() - 1;
			entryCache.put(entry, id);
			return id;
		}
	}

	public void remove(T entry) {
		entries.remove(entry);
		entryCache = null;
	}

	public int getId(T entry) {
		ensureCache();
		Integer id = entryCache.get(entry);
		if (id != null)
			return id;
		else
			return -1;
	}

	public void ensureCache() {
		if (entryCache == null) {
			entryCache = new HashMap<T, Integer>();
			int id = 0;
			for (T entry : entries) {
				entryCache.put(entry, id++);
			}
		}
	}

	public T getEntry(int id) {
		return entries.get(id);
	}

	public Set<T> getAllEntries() {
		Set<T> allEntries = new LinkedHashSet<T>();
		allEntries.addAll(entries);
		return allEntries;
	}

	public Set<T> getAllMatchings(IMatcher<T> m) {
		Set<T> matchings = new LinkedHashSet<T>();

		for (T entry : getAllEntries()) {
			if (m.matches(entry)) {
				matchings.add(entry);
			}
		}

		return matchings;
	}

	public boolean contains(T entry) {
		ensureCache();
		return entryCache.containsKey(entry);
	}

	public int size() {
		return entries.size();
	}

	public void clear() {
		entries.clear();
		entryCache.clear();
	}

	@Override
	public String toString() {
		String out = "[";

		for (T entry : entries) {
			out += entry + ",\n";
		}

		return out + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return entries.hashCode() + 17;
	}
}
