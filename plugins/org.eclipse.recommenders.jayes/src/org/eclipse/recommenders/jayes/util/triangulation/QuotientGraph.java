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

    private final Graph variables;
    private final Graph variablesToElements;

    private final Set<Integer>[] neighborCache;

    @SuppressWarnings("unchecked")
    public QuotientGraph(Graph graph) {
        this.variables = graph.clone();
        this.variablesToElements = new Graph();
        variablesToElements.initialize(variables.getAdjacency().size());
        neighborCache = new Set[variables.getAdjacency().size()];
    }

    public Set<Integer> getNeighbors(int variable) {
        if (neighborCache[variable] != null) {
            return neighborCache[variable];
        }
        Set<Integer> neighbors = new LinkedHashSet<Integer>(variables.getNeighbors(variable));
        for (Integer neighbor : variablesToElements.getNeighbors(variable)) {
            neighbors.addAll(variablesToElements.getNeighbors(neighbor));
        }
        neighbors.remove(variable);
        neighborCache[variable] = neighbors;
        return neighborCache[variable];
    }

    public void eliminate(int variable) {
        Set<Integer> neighbors = getNeighbors(variable);
        neighborCache[variable] = null;
        for (Integer neighbor : neighbors) {
            neighborCache[neighbor] = null;
        }

        for (Integer elementNeighbor : variablesToElements.getNeighbors(variable)) { // merge eliminated nodes
            merge(variablesToElements, variable, elementNeighbor);
        }
        for (Integer neighbor : variables.getNeighbors(variable)) { // interconnect neighbors
            variablesToElements.addEdge(variable, neighbor);
        }
        virtualRemoveNode(variables, variable);
    }

    private void merge(Graph graph, int v1, int v2) {
        for (int e2 : graph.getNeighbors(v2)) {
            if (v1 != e2) {
                graph.addEdge(v1, e2);
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
