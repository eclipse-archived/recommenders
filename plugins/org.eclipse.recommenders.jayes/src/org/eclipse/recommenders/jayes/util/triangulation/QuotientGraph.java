/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.jayes.util.triangulation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.recommenders.jayes.util.Graph;

/**
 * Quotient graphs are special data structures for the perfect elimination order problem. Their size stays in O(|E|)
 * where E is the set of edges. Using plain graphs would result in a storage complexity of O(|E*|), where E* is the set
 * of Edges united with the set of "fill-in" edges generated during elimination. <br/>
 * <br/>
 * See "An Approximate Minimum Degree Ordering Algorithm" (Amestoy et al. 1996)
 */
public class QuotientGraph {

    private final Graph graph;
    private final boolean[] isElement;

    private final Set<Integer>[] neighborCache;

    @SuppressWarnings("unchecked")
    public QuotientGraph(Graph graph) {
        this.graph = graph.clone();
        isElement = new boolean[graph.numberOfVertices()];
        neighborCache = new Set[graph.numberOfVertices()];
    }

    public Set<Integer> getNeighbors(int variable) {
        if (neighborCache[variable] != null) {
            return neighborCache[variable];
        }
        Set<Integer> neighbors = new LinkedHashSet<Integer>();
        if (!isElement[variable]) {
            for (int neighbor : graph.getNeighbors(variable)) {
                if (isElement[neighbor]) {
                    neighbors.addAll(graph.getNeighbors(neighbor));
                } else {
                    neighbors.add(neighbor);
                }
            }
            neighbors.remove(variable);
        }
        neighborCache[variable] = neighbors;
        return neighborCache[variable];
    }

    public void eliminate(int variable) {
        if (isElement[variable]) {
            throw new IllegalArgumentException("variable already eliminated");
        }

        Set<Integer> neighbors = getNeighbors(variable);
        neighborCache[variable] = null;
        for (Integer neighbor : neighbors) {
            neighborCache[neighbor] = null;
        }

        for (int elementNeighbor : new ArrayList<Integer>(graph.getNeighbors(variable))) { // merge eliminated nodes
            if (isElement[elementNeighbor]) {
                merge(graph, variable, elementNeighbor);
            }
        }

        isElement[variable] = true;
    }

    private void merge(Graph graph, int v1, int v2) {
        for (int n : graph.getNeighbors(v2)) {
            if (v1 != n) {
                graph.addEdge(v1, n);
            }
        }
        virtualRemoveNode(graph, v2);
    }

    // isolating node = virtually removing it
    private void virtualRemoveNode(Graph graph, final int node) {
        graph.removeIncidentEdges(node);
    }

    // TODO indistinguishable variables and external degree
}
