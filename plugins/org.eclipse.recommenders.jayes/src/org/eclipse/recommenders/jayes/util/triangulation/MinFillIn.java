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

import java.util.HashSet;
import java.util.Set;

public class MinFillIn implements IEliminationHeuristic {

    @Override
    public int getHeuristicValue(QuotientGraph graph, int node) {
        int fillIn = 0;
        Set<Integer> neighborsOfNode = graph.getNeighbors(node);
        for (final int neighbor : neighborsOfNode) {
            final Set<Integer> neighbors2 = new HashSet<Integer>(graph.getNeighbors(neighbor));

            neighbors2.retainAll(neighborsOfNode);
            fillIn += neighborsOfNode.size() - 1 - neighbors2.size();
            // Edges are counted twice, but this is okay, since the
            // ordering is maintained

        }
        return fillIn;
    }
}
