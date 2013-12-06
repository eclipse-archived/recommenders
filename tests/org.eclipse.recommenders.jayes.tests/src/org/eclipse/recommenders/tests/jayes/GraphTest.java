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
package org.eclipse.recommenders.tests.jayes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.recommenders.jayes.util.Graph;
import org.eclipse.recommenders.jayes.util.Graph.Edge;
import org.junit.Test;

public class GraphTest {

    /**
     * <pre>
     * 2
     * |
     * 0--1
     * | /
     * 3/
     * </pre>
     */
    public static Graph createTestGraph() {
        Graph graph = new Graph();
        graph.initialize(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 3);
        graph.addEdge(3, 0);
        graph.addEdge(0, 2);
        return graph;
    }

    @Test
    public void testGetNeighbors() {
        Graph g = createTestGraph();

        assertThat(g.getNeighbors(0), hasItems(1, 2, 3));
        assertThat(g.getNeighbors(1), hasItems(0, 3));
        assertThat(g.getNeighbors(2), hasItems(0));
        assertThat(g.getNeighbors(3), hasItems(0, 1));

        assertThat(g.getNeighbors(0).size(), is(3));
        assertThat(g.getNeighbors(1).size(), is(2));
        assertThat(g.getNeighbors(2).size(), is(1));
        assertThat(g.getNeighbors(3).size(), is(2));
    }

    @Test
    public void testAddEdge() {
        Graph g = createTestGraph();

        assertThat(g.getNeighbors(1), not(hasItem(2)));
        assertThat(g.getNeighbors(2), not(hasItem(1)));

        g.addEdge(1, 2);

        assertThat(g.getNeighbors(1), hasItem(2));
        assertThat(g.getNeighbors(2), hasItem(1));
    }

    @Test
    public void testRemoveEdge() {
        Graph g = createTestGraph();

        assertThat(g.getNeighbors(1), hasItem(3));
        assertThat(g.getNeighbors(3), hasItem(1));

        g.removeEdge(new Edge(1, 3).initializeBackEdge());

        assertThat(g.getNeighbors(1), not(hasItem(3)));
        assertThat(g.getNeighbors(3), not(hasItem(1)));

    }

    @Test
    public void testRemoveIncidentEdges() {
        Graph g = createTestGraph();

        assertThat(g.getNeighbors(0), hasItems(1, 2, 3));

        g.removeIncidentEdges(0);

        assertThat(g.getNeighbors(1), not(hasItem(0)));
        assertThat(g.getNeighbors(2), not(hasItem(0)));
        assertThat(g.getNeighbors(3), not(hasItem(0)));
        assertThat(g.getNeighbors(0), not(anyOf(hasItem(0), hasItem(1), hasItem(2), hasItem(3))));

    }

}
