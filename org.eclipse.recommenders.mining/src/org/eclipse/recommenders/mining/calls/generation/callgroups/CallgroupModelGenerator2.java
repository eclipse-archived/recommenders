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
package org.eclipse.recommenders.mining.calls.generation.callgroups;

import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MAX;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.safeDivMaxMin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.mining.Observation;
import org.eclipse.recommenders.commons.mining.Pattern;
import org.eclipse.recommenders.commons.mining.dictionary.Dictionary;
import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.Feature;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.mining.calls.generation.GenericNetworkBuilder;
import org.eclipse.recommenders.mining.calls.generation.IModelGenerator;
import org.eclipse.recommenders.mining.calls.generation.ObjectUsageImporter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class CallgroupModelGenerator2 implements IModelGenerator {

	private final ObjectUsageImporter importer;
	private final GenericNetworkBuilder networkBuilder;

	private Dictionary<Feature> dictionary;
	private Map<Set<CallFeature>, Integer> callgroupCounts;

	private Map<Set<CallFeature>, Map<ContextFeature, Integer>> contextCounts;
	private Map<Set<CallFeature>, Map<KindFeature, Integer>> kindCounts;
	private Map<Set<CallFeature>, Map<DefinitionFeature, Integer>> definitionCounts;
	private List<Pattern> patterns;

	@Inject
	public CallgroupModelGenerator2(ObjectUsageImporter importer, GenericNetworkBuilder networkBuilder) {
		this.importer = importer;
		this.networkBuilder = networkBuilder;

		callgroupCounts = Maps.newHashMap();
		contextCounts = Maps.newHashMap();
		kindCounts = Maps.newHashMap();
		definitionCounts = Maps.newHashMap();
	}

	// @Override
	// public void generate(ITypeName type, Collection<ObjectUsage> usages, IModelArchiveWriter writer) throws
	// IOException {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public BayesianNetwork generate(ITypeName type, Collection<ObjectUsage> usages) {

		System.out.println(String.format("learning %s... (%d usages)", type, usages.size()));
		List<Observation> observations = transform(usages);

		System.out.println("... building dictionary");

		dictionary = collectFeatures(observations);
		System.out.println("... analyzing");
		countFeatures(observations);
		System.out.println("... calculating patterns");
		patterns = createPatterns();
		System.out.println("... found " + patterns.size() + " patterns");
		BayesianNetwork genericNetwork = networkBuilder.buildNetwork(type, patterns, dictionary);

		return genericNetwork;
	}

	private List<Observation> transform(Collection<ObjectUsage> usages) {
		List<Observation> observations = Lists.newArrayList();

		for (ObjectUsage usage : usages) {
			Observation observation = importer.transform(usage);
			observations.add(observation);
		}

		return observations;
	}

	private Dictionary<Feature> collectFeatures(List<Observation> observations) {
		Dictionary<Feature> d = new Dictionary<Feature>();
		for (Observation o : observations) {
			d.add(o.getContext());
			d.add(o.getKind());
			d.add(o.getDefinition());
			for (CallFeature f : o.getCalls()) {
				d.add(f);
			}
		}
		return d;
	}

	private void countFeatures(List<Observation> observations) {
		callgroupCounts.clear();
		contextCounts.clear();
		kindCounts.clear();
		definitionCounts.clear();

		for (Observation o : observations) {
			Set<CallFeature> callgroup = o.getCalls();
			count(callgroup);
			count(callgroup, o.getContext(), contextCounts);
			count(callgroup, o.getKind(), kindCounts);
			count(callgroup, o.getDefinition(), definitionCounts);
		}
	}

	private void count(Set<CallFeature> callgroup) {
		Integer count = callgroupCounts.get(callgroup);
		if (count == null) {
			count = 1;
		} else
			count++;
		callgroupCounts.put(callgroup, count);
	}

	private <T> void count(Set<CallFeature> callgroup, T feature, Map<Set<CallFeature>, Map<T, Integer>> allCounts) {
		Map<T, Integer> featureCounts = allCounts.get(callgroup);
		if (featureCounts == null) {
			featureCounts = Maps.newHashMap();
			allCounts.put(callgroup, featureCounts);
		}

		Integer count = featureCounts.get(feature);
		if (count == null) {
			count = 1;
		} else {
			count = count + 1;
		}
		featureCounts.put(feature, count);
	}

	private List<Pattern> createPatterns() {

		List<Pattern> patterns = Lists.newArrayList();

		Integer index = 0;
		for (Set<CallFeature> callgroup : callgroupCounts.keySet()) {
			int totalCount = callgroupCounts.get(callgroup);

			Pattern p = new Pattern();
			p.setName("p" + (index++));
			p.setNumberOfObservations(totalCount);

			for (CallFeature f : callgroup) {
				p.setProbability(f, P_MAX);
			}

			Map<ContextFeature, Integer> contexts = contextCounts.get(callgroup);
			for (ContextFeature f : contexts.keySet()) {
				int count = contexts.get(f);
				double probability = safeDivMaxMin(count, totalCount);
				p.setProbability(f, probability);
			}

			Map<KindFeature, Integer> kinds = kindCounts.get(callgroup);
			for (KindFeature f : kinds.keySet()) {
				int count = kinds.get(f);
				double probability = safeDivMaxMin(count, totalCount);
				p.setProbability(f, probability);
			}

			Map<DefinitionFeature, Integer> definitions = definitionCounts.get(callgroup);
			for (DefinitionFeature f : definitions.keySet()) {
				int count = definitions.get(f);
				double probability = safeDivMaxMin(count, totalCount);
				p.setProbability(f, probability);
			}

			patterns.add(p);
		}

		return patterns;
	}
}