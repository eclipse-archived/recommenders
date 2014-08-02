/**
 * Copyright (c) 2011 Michael Kutschke. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.jayes.inference.jtree;

import static org.eclipse.recommenders.jayes.util.Pair.newPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.recommenders.internal.jayes.util.ArrayUtils;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.factor.AbstractFactor;
import org.eclipse.recommenders.jayes.factor.arraywrapper.DoubleArrayWrapper;
import org.eclipse.recommenders.jayes.factor.arraywrapper.IArrayWrapper;
import org.eclipse.recommenders.jayes.inference.AbstractInferer;
import org.eclipse.recommenders.jayes.util.Graph;
import org.eclipse.recommenders.jayes.util.MathUtils;
import org.eclipse.recommenders.jayes.util.NumericalInstabilityException;
import org.eclipse.recommenders.jayes.util.OrderIgnoringPair;
import org.eclipse.recommenders.jayes.util.Pair;
import org.eclipse.recommenders.jayes.util.sharing.CanonicalArrayWrapperManager;
import org.eclipse.recommenders.jayes.util.sharing.CanonicalIntArrayManager;
import org.eclipse.recommenders.jayes.util.triangulation.MinFillIn;

@SuppressWarnings("deprecation")
public class JunctionTreeAlgorithm extends AbstractInferer {

    private static final double ONE = 1.0;
    private static final double ONE_LOG = 0.0;

    protected Map<OrderIgnoringPair<Integer>, AbstractFactor> sepSets;
    protected Graph junctionTree;
    protected AbstractFactor[] nodePotentials;
    protected Map<Pair<Integer, Integer>, int[]> preparedMultiplications;

    // mapping from variables to clusters that contain them
    protected int[][] concernedClusters;
    protected AbstractFactor[] queryFactors;
    protected int[][] preparedQueries;
    protected boolean[] isBeliefValid;
    protected List<Pair<AbstractFactor, IArrayWrapper>> initializations;

    protected int[][] queryFactorReverseMapping;

    // used for computing evidence collection skip
    protected Set<Integer> clustersHavingEvidence;
    protected boolean[] isObserved;

    protected double[] scratchpad;

    protected JunctionTreeBuilder junctionTreeBuilder = JunctionTreeBuilder.forHeuristic(new MinFillIn());

    public void setJunctionTreeBuilder(JunctionTreeBuilder bldr) {
        this.junctionTreeBuilder = bldr;
    }

    @Override
    public double[] getBeliefs(final BayesNode node) {
        if (!beliefsValid) {
            beliefsValid = true;
            updateBeliefs();
        }
        final int nodeId = node.getId();
        if (!isBeliefValid[nodeId]) {
            isBeliefValid[nodeId] = true;
            if (!evidence.containsKey(node)) {
                validateBelief(nodeId);
            } else {
                Arrays.fill(beliefs[nodeId], 0);
                beliefs[nodeId][node.getOutcomeIndex(evidence.get(node))] = 1;
            }
        }
        return super.getBeliefs(node);
    }

    private void validateBelief(final int nodeId) {
        final AbstractFactor f = queryFactors[nodeId];
        // TODO change beliefs to ArrayWrappers
        f.sumPrepared(new DoubleArrayWrapper(beliefs[nodeId]), preparedQueries[nodeId]);
        if (f.isLogScale()) {
            MathUtils.exp(beliefs[nodeId]);
        }
        try {
            beliefs[nodeId] = MathUtils.normalize(beliefs[nodeId]);
        } catch (final IllegalArgumentException exception) {
            throw new NumericalInstabilityException("Numerical instability detected for evidence: " + evidence
                    + " and node : " + nodeId
                    + ", consider using logarithmic scale computation (configurable in FactorFactory)", exception);
        }
    }

    @Override
    protected void updateBeliefs() {
        Arrays.fill(isBeliefValid, false);
        doUpdateBeliefs();
    }

    private void doUpdateBeliefs() {

        incorporateAllEvidence();
        int propagationRoot = findPropagationRoot();

        replayFactorInitializations();
        collectEvidence(propagationRoot, skipCollection(propagationRoot));
        distributeEvidence(propagationRoot, skipDistribution(propagationRoot));
    }

    private void replayFactorInitializations() {
        for (final Pair<AbstractFactor, IArrayWrapper> init : initializations) {
            init.getFirst().copyValues(init.getSecond());
        }
    }

    private void incorporateAllEvidence() {
        for (Pair<AbstractFactor, IArrayWrapper> init : initializations) {
            init.getFirst().resetSelections();
        }

        clustersHavingEvidence.clear();
        Arrays.fill(isObserved, false);
        for (BayesNode n : evidence.keySet()) {
            incorporateEvidence(n);
        }
    }

    private void incorporateEvidence(final BayesNode node) {
        int n = node.getId();
        isObserved[n] = true;
        // get evidence to all concerned factors (includes home cluster)
        for (final Integer concernedCluster : concernedClusters[n]) {
            nodePotentials[concernedCluster].select(n, node.getOutcomeIndex(evidence.get(node)));
            clustersHavingEvidence.add(concernedCluster);
        }
    }

    private int findPropagationRoot() {
        int propagationRoot = 0;
        for (BayesNode n : evidence.keySet()) {
            propagationRoot = concernedClusters[n.getId()][0];
        }
        return propagationRoot;
    }

    /**
     * checks which nodes need not be processed during collectEvidence (because of preprocessing). These are those nodes
     * without evidence which are leaves or which only have non-evidence descendants
     *
     * @param root
     *            the node to start the check from
     * @return a set of the nodes not needing a call of collectEvidence
     */
    private Set<Integer> skipCollection(final int root) {
        final Set<Integer> skipped = new HashSet<Integer>(nodePotentials.length);
        recursiveSkipCollection(root, new HashSet<Integer>(nodePotentials.length), skipped);
        return skipped;
    }

    private void recursiveSkipCollection(final int node, final Set<Integer> visited, final Set<Integer> skipped) {
        visited.add(node);
        boolean areAllDescendantsSkipped = true;
        for (final int neighbor : junctionTree.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                recursiveSkipCollection(neighbor, visited, skipped);
                if (!skipped.contains(neighbor)) {
                    areAllDescendantsSkipped = false;
                }
            }
        }
        if (areAllDescendantsSkipped && !clustersHavingEvidence.contains(node)) {
            skipped.add(node);
        }
    }

    /**
     * checks which nodes do not need to be visited during evidence distribution. These are exactly those nodes which
     * are
     * <ul>
     * <li>not the query factor of a non-evidence variable</li>
     * <li>AND have no descendants that cannot be skipped</li>
     * </ul>
     *
     * @param distNode
     * @return
     */
    private Set<Integer> skipDistribution(final int distNode) {
        final Set<Integer> skipped = new HashSet<Integer>(nodePotentials.length);
        recursiveSkipDistribution(distNode, new HashSet<Integer>(nodePotentials.length), skipped);
        return skipped;
    }

    private void recursiveSkipDistribution(final int node, final Set<Integer> visited, final Set<Integer> skipped) {
        visited.add(node);
        boolean areAllDescendantsSkipped = true;
        for (final Integer neighbor : junctionTree.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                recursiveSkipDistribution(neighbor, visited, skipped);
                if (!skipped.contains(neighbor)) {
                    areAllDescendantsSkipped = false;
                }
            }
        }
        if (areAllDescendantsSkipped && !isQueryFactorOfUnobservedVariable(node)) {
            skipped.add(node);
        }
    }

    private boolean isQueryFactorOfUnobservedVariable(final int node) {
        for (int i : queryFactorReverseMapping[node]) {
            if (!isObserved[i]) {
                return true;
            }
        }
        return false;
    }

    private void collectEvidence(final int cluster, final Set<Integer> marked) {
        marked.add(cluster);
        for (final int n : junctionTree.getNeighbors(cluster)) {
            if (!marked.contains(n)) {
                collectEvidence(n, marked);
                messagePass(n, cluster);
            }
        }
    }

    private void distributeEvidence(final int cluster, final Set<Integer> marked) {
        marked.add(cluster);
        for (final int n : junctionTree.getNeighbors(cluster)) {
            if (!marked.contains(n)) {
                messagePass(cluster, n);
                distributeEvidence(n, marked);
            }
        }
    }

    private void messagePass(final int v1, int v2) {

        OrderIgnoringPair<Integer> sepSetEdge = new OrderIgnoringPair<Integer>(v1, v2);

        final AbstractFactor sepSet = sepSets.get(sepSetEdge);
        if (!needMessagePass(sepSet)) {
            return;
        }

        final IArrayWrapper newSepValues = sepSet.getValues();
        System.arraycopy(newSepValues.toDoubleArray(), 0, scratchpad, 0, newSepValues.length());

        final int[] preparedOp = preparedMultiplications.get(Pair.newPair(v2, v1));
        nodePotentials[sepSetEdge.getFirst()].sumPrepared(newSepValues, preparedOp);

        if (isOnlyFirstLogScale(sepSetEdge)) {
            MathUtils.exp(newSepValues);
        }
        if (areBothEndsLogScale(sepSetEdge)) {
            MathUtils.secureSubtract(newSepValues.toDoubleArray(), scratchpad, scratchpad);
        } else {
            MathUtils.secureDivide(newSepValues.toDoubleArray(), scratchpad, scratchpad);
        }

        if (isOnlySecondLogScale(sepSetEdge)) {
            MathUtils.log(scratchpad);
        }
        // TODO scratchpad -> ArrayWrapper
        nodePotentials[sepSetEdge.getSecond()].multiplyPrepared(new DoubleArrayWrapper(scratchpad),
                preparedMultiplications.get(Pair.newPair(v1, v2)));
    }

    /*
     * we don't get additional information if all variables in the sepSet are observed, so skip message pass
     */
    private boolean needMessagePass(final AbstractFactor sepSet) {
        for (final int var : sepSet.getDimensionIDs()) {
            if (!isObserved[var]) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnlyFirstLogScale(final OrderIgnoringPair<Integer> edge) {
        return nodePotentials[edge.getFirst()].isLogScale() && !nodePotentials[edge.getSecond()].isLogScale();
    }

    private boolean isOnlySecondLogScale(final OrderIgnoringPair<Integer> edge) {
        return !nodePotentials[edge.getFirst()].isLogScale() && nodePotentials[edge.getSecond()].isLogScale();
    }

    @Override
    public void setNetwork(final BayesNet net) {
        super.setNetwork(net);
        initializeFields(net.getNodes().size());
        JunctionTree jtree = buildJunctionTree(net);
        Map<AbstractFactor, Integer> homeClusters = computeHomeClusters(net, jtree.getClusters());
        initializeClusterFactors(net, jtree.getClusters(), homeClusters);
        initializeSepsetFactors(jtree.getSepSets());
        determineConcernedClusters();
        setQueryFactors();
        initializePotentialValues();
        multiplyCPTsIntoPotentials(net, homeClusters);
        prepareMultiplications();
        prepareScratch();
        invokeInitialBeliefUpdate();
        storePotentialValues();
    }

    @SuppressWarnings("unchecked")
    private void determineConcernedClusters() {
        concernedClusters = new int[queryFactors.length][];
        List<Integer>[] temp = new List[concernedClusters.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new ArrayList<Integer>();
        }

        for (int i = 0; i < nodePotentials.length; i++) {
            int[] dimensionIDs = nodePotentials[i].getDimensionIDs();
            for (final int var : dimensionIDs) {
                temp[var].add(i);
            }
        }

        for (int i = 0; i < temp.length; i++) {
            concernedClusters[i] = ArrayUtils.toIntArray(temp[i]);
        }
    }

    private void initializeFields(int numNodes) {
        isBeliefValid = new boolean[beliefs.length];
        Arrays.fill(isBeliefValid, false);
        queryFactors = new AbstractFactor[numNodes];
        preparedQueries = new int[numNodes][];
        sepSets = new HashMap<OrderIgnoringPair<Integer>, AbstractFactor>(numNodes);
        preparedMultiplications = new HashMap<Pair<Integer, Integer>, int[]>(numNodes);
        initializations = new ArrayList<Pair<AbstractFactor, IArrayWrapper>>();
        clustersHavingEvidence = new HashSet<Integer>(numNodes);
        isObserved = new boolean[numNodes];
    }

    private JunctionTree buildJunctionTree(BayesNet net) {
        final JunctionTree jtree = junctionTreeBuilder.buildJunctionTree(net);
        this.junctionTree = jtree.getGraph();

        return jtree;
    }

    private Map<AbstractFactor, Integer> computeHomeClusters(BayesNet net, final List<List<Integer>> clusters) {
        Map<AbstractFactor, Integer> homeClusters = new HashMap<AbstractFactor, Integer>();
        for (final BayesNode node : net.getNodes()) {
            final int[] nodeAndParents = node.getFactor().getDimensionIDs();
            for (final ListIterator<List<Integer>> clusterIt = clusters.listIterator(); clusterIt.hasNext();) {
                if (containsAll(clusterIt.next(), nodeAndParents)) {
                    homeClusters.put(node.getFactor(), clusterIt.nextIndex() - 1);
                    break;
                }
            }
        }
        return homeClusters;
    }

    private boolean containsAll(List<Integer> list, int[] ints) {
        for (int n : ints) {
            if (!list.contains(n)) {
                return false;
            }
        }
        return true;
    }

    private void initializeClusterFactors(BayesNet net, final List<List<Integer>> clusters,
            Map<AbstractFactor, Integer> homeClusters) {
        nodePotentials = new AbstractFactor[clusters.size()];
        Map<Integer, List<AbstractFactor>> multiplicationPartners = findMultiplicationPartners(net, homeClusters);
        for (final ListIterator<List<Integer>> cliqueIt = clusters.listIterator(); cliqueIt.hasNext();) {
            final List<Integer> cluster = cliqueIt.next();
            int current = cliqueIt.nextIndex() - 1;
            List<AbstractFactor> multiplicationPartnerList = multiplicationPartners.get(current);
            final AbstractFactor cliqueFactor = factory.create(cluster,
                    multiplicationPartnerList == null ? Collections.<AbstractFactor>emptyList()
                            : multiplicationPartnerList);
            nodePotentials[current] = cliqueFactor;
        }
    }

    private Map<Integer, List<AbstractFactor>> findMultiplicationPartners(BayesNet net,
            Map<AbstractFactor, Integer> homeClusters) {
        Map<Integer, List<AbstractFactor>> potentialMap = new HashMap<Integer, List<AbstractFactor>>();
        for (final BayesNode node : net.getNodes()) {
            final Integer nodeHome = homeClusters.get(node.getFactor());
            if (!potentialMap.containsKey(nodeHome)) {
                potentialMap.put(nodeHome, new ArrayList<AbstractFactor>());
            }
            potentialMap.get(nodeHome).add(node.getFactor());
        }
        return potentialMap;
    }

    private void initializeSepsetFactors(final List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> sepSets) {
        for (final Pair<OrderIgnoringPair<Integer>, List<Integer>> sep : sepSets) {
            this.sepSets.put(sep.getFirst(), factory.create(sep.getSecond(), Collections.<AbstractFactor>emptyList()));
        }
    }

    private void setQueryFactors() {
        for (int i = 0; i < queryFactors.length; i++) {
            for (final Integer f : concernedClusters[i]) {
                final boolean isFirstOrSmallerTable = queryFactors[i] == null
                        || queryFactors[i].getValues().length() > nodePotentials[f].getValues().length();
                        if (isFirstOrSmallerTable) {
                            queryFactors[i] = nodePotentials[f];
                        }
            }
        }

        queryFactorReverseMapping = new int[nodePotentials.length][];
        for (int i = 0; i < nodePotentials.length; i++) {
            List<Integer> queryVars = new ArrayList<Integer>();
            for (int var : nodePotentials[i].getDimensionIDs()) {
                if (queryFactors[var] == nodePotentials[i]) {
                    queryVars.add(var);
                }
            }
            queryFactorReverseMapping[i] = ArrayUtils.toIntArray(queryVars);
        }
    }

    private void prepareMultiplications() {
        // compress by combining equal prepared statements, thus saving memory
        final CanonicalIntArrayManager flyWeight = new CanonicalIntArrayManager();
        prepareSepsetMultiplications(flyWeight);
        prepareQueries(flyWeight);
    }

    private void prepareSepsetMultiplications(final CanonicalIntArrayManager flyWeight) {
        for (int node = 0; node < nodePotentials.length; node++) {
            for (final int n : junctionTree.getNeighbors(node)) {
                final int[] preparedMultiplication = nodePotentials[n].prepareMultiplication(sepSets
                        .get(new OrderIgnoringPair<Integer>(node, n)));
                preparedMultiplications.put(Pair.newPair(node, n), flyWeight.getInstance(preparedMultiplication));
            }
        }
    }

    private void prepareQueries(final CanonicalIntArrayManager flyWeight) {
        for (int i = 0; i < queryFactors.length; i++) {
            final AbstractFactor beliefFactor = factory.create(Arrays.asList(i),
                    Collections.<AbstractFactor>emptyList());
            final int[] preparedQuery = queryFactors[i].prepareMultiplication(beliefFactor);
            preparedQueries[i] = flyWeight.getInstance(preparedQuery);
        }
    }

    private void prepareScratch() {
        int maxSize = 0;
        for (AbstractFactor sepSet : sepSets.values()) {
            maxSize = Math.max(maxSize, sepSet.getValues().length());
        }
        scratchpad = new double[maxSize];
    }

    private void invokeInitialBeliefUpdate() {

        collectEvidence(0, new HashSet<Integer>());
        distributeEvidence(0, new HashSet<Integer>());
    }

    private void initializePotentialValues() {
        for (final AbstractFactor f : nodePotentials) {
            f.fill(f.isLogScale() ? ONE_LOG : ONE);
        }

        for (final Entry<OrderIgnoringPair<Integer>, AbstractFactor> sepSet : sepSets.entrySet()) {
            if (!areBothEndsLogScale(sepSet.getKey())) {
                // if one part is log-scale, we transform to non-log-scale
                sepSet.getValue().fill(ONE);
            } else {
                sepSet.getValue().fill(ONE_LOG);
            }
        }
    }

    private void multiplyCPTsIntoPotentials(BayesNet net, Map<AbstractFactor, Integer> homeClusters) {
        for (final BayesNode node : net.getNodes()) {
            final AbstractFactor nodeHome = nodePotentials[homeClusters.get(node.getFactor())];
            if (nodeHome.isLogScale()) {
                nodeHome.multiplyCompatibleToLog(node.getFactor());
            } else {
                nodeHome.multiplyCompatible(node.getFactor());
            }
        }
    }

    private boolean areBothEndsLogScale(final OrderIgnoringPair<Integer> edge) {
        return nodePotentials[edge.getFirst()].isLogScale() && nodePotentials[edge.getSecond()].isLogScale();
    }

    private void storePotentialValues() {
        CanonicalArrayWrapperManager flyweight = new CanonicalArrayWrapperManager();
        for (final AbstractFactor pot : nodePotentials) {
            initializations.add(newPair(pot, flyweight.getInstance(pot.getValues().clone())));
        }

        for (final AbstractFactor sep : sepSets.values()) {
            initializations.add(newPair(sep, flyweight.getInstance(sep.getValues().clone())));
        }
    }
}
