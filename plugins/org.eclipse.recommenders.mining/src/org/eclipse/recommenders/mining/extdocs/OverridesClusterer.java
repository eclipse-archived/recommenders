/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.extdocs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.mahout.clustering.canopy.Canopy;
import org.apache.mahout.clustering.canopy.CanopyClusterer;
import org.apache.mahout.clustering.kmeans.Cluster;
import org.apache.mahout.clustering.kmeans.KMeansClusterer;
import org.apache.mahout.common.distance.TanimotoDistanceMeasure;
import org.apache.mahout.common.distance.WeightedDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.eclipse.recommenders.extdoc.transport.types.MethodPattern;
import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.Option;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OverridesClusterer {

    private final static double CANOPY_T1 = 0.5d;
    private final static double CANOPY_T2 = 0.8d;
    private final static double MIN_METHOD_PROBABILITY = 0.25;
    private final int minClusterSize;
    private final WeightedDistanceMeasure distanceMeasure = new TanimotoDistanceMeasure();
    private LinkedHashMap<String, Integer> features2Ids;
    private BiMap<String, Integer> features2IdsIndex;;
    private String[] featureNames;
    private Bag<Set<IMethodName>> rawData;

    public OverridesClusterer(final int minClusterSize) {
        this.minClusterSize = minClusterSize;
    }

    public List<MethodPattern> cluster(final Bag<Set<IMethodName>> rawData) {
        this.rawData = rawData;
        initialize();
        final List<Canopy> canopies = doCanopyClustering();
        final List<Cluster> clusterInitialization = createClusterInitialization(canopies);
        final List<Cluster> clusteringResult = doKMeansClustering(clusterInitialization);
        return createMethodPatterns(clusteringResult);
    }

    private void initialize() {
        features2Ids = Maps.newLinkedHashMap();
        features2IdsIndex = HashBiMap.create();
        for (final Set<IMethodName> observation : rawData) {
            initializeFeatureIds(observation);
        }
        featureNames = Iterables.toArray(features2Ids.keySet(), String.class);
    }

    private void initializeFeatureIds(final Set<IMethodName> observation) {
        for (final IMethodName overriddenMethod : observation) {
            if (overriddenMethod.isInit()) {
                continue;
            }
            findOrCreateFeature(overriddenMethod.getIdentifier());
        }
    }

    private List<Vector> getVectors() {
        final List<Vector> vectors = Lists.newLinkedList();
        for (final Set<IMethodName> observation : rawData) {
            final RandomAccessSparseVector vector = createVector(observation);
            for (int i = 0; i < rawData.count(observation); i++) {
                vectors.add(vector);
            }
        }
        return vectors;
    }

    private RandomAccessSparseVector createVector(final Set<IMethodName> observation) {
        final RandomAccessSparseVector v = new RandomAccessSparseVector(features2Ids.size());

        for (final IMethodName overriddenMethod : observation) {
            if (overriddenMethod.isInit()) {
                continue;
            }
            final int id = findOrCreateFeature(overriddenMethod.getIdentifier());
            v.set(id, 1.0d);
        }
        return v;
    }

    private int findOrCreateFeature(final String defFeatureName) {
        if (!features2Ids.containsKey(defFeatureName)) {
            final int id = features2Ids.size();
            features2Ids.put(defFeatureName, id);
            features2IdsIndex.put(defFeatureName, id);
        }
        final int newId = features2Ids.get(defFeatureName);
        return newId;
    }

    private List<Canopy> doCanopyClustering() {
        final List<Canopy> canopies = CanopyClusterer.createCanopies(getVectors(), distanceMeasure, CANOPY_T1,
                CANOPY_T2);
        return canopies;
    }

    private List<Cluster> createClusterInitialization(final List<Canopy> canopies) {
        final List<Cluster> clusters = new ArrayList<Cluster>();
        int i = 0;
        for (final Canopy canopy : canopies) {

            final Cluster c = new Cluster(canopy.getCenter(), i++, distanceMeasure);
            clusters.add(c);
        }
        return clusters;
    }

    private List<Cluster> doKMeansClustering(final List<Cluster> clusters) {
        final List<List<Cluster>> iterations = KMeansClusterer.clusterPoints(getVectors(), clusters, distanceMeasure,
                50, 0.4d);
        return iterations.get(iterations.size() - 1);
    }

    private List<MethodPattern> createMethodPatterns(final List<Cluster> cluster) {
        final LinkedList<MethodPattern> methodPatterns = Lists.newLinkedList();
        for (final Cluster c : cluster) {
            if (c.count() >= minClusterSize) {
                final Option<MethodPattern> methodPattern = createMethodPattern(c);
                if (methodPattern.hasValue()) {
                    methodPatterns.add(methodPattern.get());
                }
            }
        }
        return methodPatterns;
    }

    private Option<MethodPattern> createMethodPattern(final Cluster c) {
        final TreeMap<IMethodName, Double> group = Maps.newTreeMap();
        for (final Iterator<Element> it = c.getCenter().iterateNonZero(); it.hasNext();) {
            final Element next = it.next();
            final double d = next.get();
            if (d < MIN_METHOD_PROBABILITY) {
                continue;
            }
            final String name = featureNames[next.index()];
            group.put(VmMethodName.get(name), d);
        }
        if (!group.isEmpty()) {
            return Option.wrap(MethodPattern.create(c.count(), group));
        }
        return Option.none();
    }

}
