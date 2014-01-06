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
package org.eclipse.recommenders.jayes.inference.jtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.util.Graph;
import org.eclipse.recommenders.jayes.util.OrderIgnoringPair;
import org.eclipse.recommenders.jayes.util.Pair;
import org.eclipse.recommenders.jayes.util.triangulation.GraphElimination;
import org.eclipse.recommenders.jayes.util.triangulation.IEliminationHeuristic;

public class JunctionTreeBuilder {

    private final IEliminationHeuristic heuristic;

    public static JunctionTreeBuilder forHeuristic(IEliminationHeuristic heuristic) {
        return new JunctionTreeBuilder(heuristic);
    }

    protected JunctionTreeBuilder(IEliminationHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public JunctionTree buildJunctionTree(BayesNet net) {
        JunctionTree junctionTree = new JunctionTree();
        junctionTree.setClusters(triangulateGraphAndFindCliques(buildMoralGraph(net), weightNodesByOutcomes(net),
                heuristic));
        junctionTree.setSepSets(computeSepsets(junctionTree, net));
        return junctionTree;
    }

    private Graph buildMoralGraph(BayesNet net) {
        Graph moral = new Graph(net.getNodes().size());
        for (final BayesNode node : net.getNodes()) {
            addMoralEdges(moral, node);
        }
        return moral;
    }

    private void addMoralEdges(Graph moral, final BayesNode node) {
        final ListIterator<BayesNode> it = node.getParents().listIterator();
        while (it.hasNext()) {
            final BayesNode parent = it.next();
            final ListIterator<BayesNode> remainingParentsIt = node.getParents().listIterator(it.nextIndex());
            while (remainingParentsIt.hasNext()) { // connect parents
                final BayesNode otherParent = remainingParentsIt.next();
                moral.addEdge(parent.getId(), otherParent.getId());
            }
            moral.addEdge(node.getId(), parent.getId());
        }
    }

    private List<List<Integer>> triangulateGraphAndFindCliques(Graph graph, double[] weights,
            IEliminationHeuristic eliminationHeuristic) {
        GraphElimination triangulate = new GraphElimination(graph, weights, eliminationHeuristic);

        final List<List<Integer>> cliques = new ArrayList<List<Integer>>();
        for (List<Integer> nextClique : triangulate) {
            if (!containsSuperset(cliques, nextClique)) {
                cliques.add(nextClique);
            }
        }
        return cliques;
    }

    private double[] weightNodesByOutcomes(BayesNet net) {
        double[] weights = new double[net.getNodes().size()];
        for (BayesNode node : net.getNodes()) {
            weights[node.getId()] = Math.log(node.getOutcomeCount());
            // using these weights is the same as minimizing the resulting cluster factor size
            // which is given by the product of the variable outcome counts.
        }
        return weights;
    }

    private boolean containsSuperset(final Collection<? extends Collection<Integer>> sets, final Collection<Integer> set) {
        boolean isSubsetOfOther = false;
        for (final Collection<Integer> superset : sets) {
            if (superset.containsAll(set)) {
                isSubsetOfOther = true;
                break;
            }
        }
        return isSubsetOfOther;
    }

    private List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> computeSepsets(JunctionTree junctionTree, BayesNet net) {
        return new SepsetComputer().computeSepsets(junctionTree, net);

    }

}
