/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.jayes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * undirected graph
 */
public class Graph implements Cloneable {

    private final ArrayList<Integer>[] adjacency;

    public int numberOfVertices() {
        return adjacency.length;
    }

    @SuppressWarnings("unchecked")
    public Graph(final int nodes) {
        adjacency = new ArrayList[nodes];
        for (int i = 0; i < nodes; i++) {
            adjacency[i] = new ArrayList<Integer>();
        }
    }

    public void addEdge(final int v1, final int v2) {
        addOrdered(adjacency[v1], v2);
        addOrdered(adjacency[v2], v1);
    }

    private void addOrdered(List<Integer> list, int v2) {
        int i = Collections.binarySearch(list, v2);
        if (i < 0) {
            list.add(-i - 1, v2);
        }
    }

    public void removeEdge(final int v1, final int v2) {
        removeOrdered(adjacency[v1], v2);
        removeOrdered(adjacency[v2], v1);
    }

    private void removeOrdered(List<Integer> list, int v2) {
        int i = Collections.binarySearch(list, v2);
        if (i >= 0) {
            list.remove(i);
        }
    }

    public void removeIncidentEdges(int v) {
        for (int n : adjacency[v]) {
            removeOrdered(adjacency[n], v);
        }
        adjacency[v].clear();
    }

    public List<Integer> getNeighbors(int var) {
        return adjacency[var];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Graph clone() {
        try {
            Graph clone = (Graph) super.clone();
            for (int i = 0; i < adjacency.length; i++) {
                clone.adjacency[i] = (ArrayList<Integer>) adjacency[i].clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // should not happen
            throw new AssertionError(e);
        }
    }
}
