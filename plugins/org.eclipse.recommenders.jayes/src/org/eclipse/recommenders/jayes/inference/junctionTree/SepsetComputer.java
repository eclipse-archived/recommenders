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
package org.eclipse.recommenders.jayes.inference.junctionTree;

import static org.eclipse.recommenders.jayes.util.Pair.newPair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.recommenders.internal.jayes.util.UnionFind;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.util.Graph;
import org.eclipse.recommenders.jayes.util.OrderIgnoringPair;
import org.eclipse.recommenders.jayes.util.Pair;

public class SepsetComputer {

    public List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> computeSepsets(JunctionTree junctionTree, BayesNet net) {
        final List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> candidates = enumerateCandidateSepSets(junctionTree
                .getClusters());
        Collections.sort(candidates, new SepsetComparator(net));
        return computeMaxSpanningTree(junctionTree.getGraph(), candidates);

    }

    private List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> enumerateCandidateSepSets(List<List<Integer>> clusters) {
        final List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> sepSets = new ArrayList<Pair<OrderIgnoringPair<Integer>, List<Integer>>>();
        final ListIterator<List<Integer>> it = clusters.listIterator();
        while (it.hasNext()) {
            final List<Integer> clique1 = it.next();
            final ListIterator<List<Integer>> remainingIt = clusters.listIterator(it.nextIndex());
            while (remainingIt.hasNext()) { // generate sepSets
                final List<Integer> clique2 = new ArrayList<Integer>(remainingIt.next());
                clique2.retainAll(clique1);
                sepSets.add(newPair(new OrderIgnoringPair<Integer>(it.nextIndex() - 1, remainingIt.nextIndex() - 1),
                        clique2));
            }
        }
        return sepSets;
    }

    private List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> computeMaxSpanningTree(Graph graph,
            final List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> sortedCandidateSepSets) {

        final ArrayDeque<Pair<OrderIgnoringPair<Integer>, List<Integer>>> pq = new ArrayDeque<Pair<OrderIgnoringPair<Integer>, List<Integer>>>(
                sortedCandidateSepSets);

        final int vertexCount = graph.numberOfVertices();
        final UnionFind[] sets = UnionFind.createArray(vertexCount);

        final List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> leftSepSets = new ArrayList<Pair<OrderIgnoringPair<Integer>, List<Integer>>>();
        while (leftSepSets.size() < vertexCount - 1) {
            final Pair<OrderIgnoringPair<Integer>, List<Integer>> sep = pq.poll();
            final boolean bothEndsInSameTree = sets[sep.getFirst().getFirst()].find() == sets[sep.getFirst()
                    .getSecond()].find();
            if (!bothEndsInSameTree) {
                sets[sep.getFirst().getFirst()].merge(sets[sep.getFirst().getSecond()]);
                leftSepSets.add(sep);
                graph.addEdge(sep.getFirst().getFirst(), sep.getFirst().getSecond());
            }
        }
        return leftSepSets;
    }

    private final class SepsetComparator implements Comparator<Pair<OrderIgnoringPair<Integer>, List<Integer>>> {

        private final BayesNet net;

        public SepsetComparator(BayesNet net) {
            this.net = net;
        }

        // heuristic: choose sepSet with most variables first,
        // if equal, choose the on with least table size
        @Override
        public int compare(final Pair<OrderIgnoringPair<Integer>, List<Integer>> sepSet1,
                final Pair<OrderIgnoringPair<Integer>, List<Integer>> sepSet2) {
            final int compareNumberOfVariables = compare(sepSet1.getSecond().size(), sepSet2.getSecond().size());
            if (compareNumberOfVariables != 0) {
                return -compareNumberOfVariables;
            }
            final int tableSize1 = getTableSize(sepSet1.getSecond());
            final int tableSize2 = getTableSize(sepSet2.getSecond());
            return compare(tableSize1, tableSize2);

        }

        private int getTableSize(final List<Integer> cluster) {
            int tableSize = 1;
            for (final int id : cluster) {
                tableSize *= net.getNode(id).getOutcomeCount();
            }
            return tableSize;
        }

        private int compare(final int i1, final int i2) {
            return i1 - i2;
        }
    }

}
