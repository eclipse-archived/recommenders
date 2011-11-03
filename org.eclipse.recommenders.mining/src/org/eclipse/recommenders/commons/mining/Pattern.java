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
package org.eclipse.recommenders.commons.mining;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.commons.mining.features.Feature;

/**
 * container for the clustering results of a single cluster
 * 
 * @author seb
 */
public class Pattern {

	private String name = "";
	private int numberOfObservations = 0;
	private Map<Feature, Double> probabilities = new LinkedHashMap<Feature, Double>();

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setNumberOfObservations(int numberOfObservations) {
		this.numberOfObservations = numberOfObservations;
	}

	public int getNumberOfObservations() {
		return numberOfObservations;
	}

	public void setProbability(Feature feature, double probability) {
		probabilities.put(feature, probability);
	}

	public double getProbability(Feature feature) {
		Double propability = probabilities.get(feature);

		if (propability == null)
			return 0.0;
		else
			return propability;
	}

	public Pattern clone(String nameOfNewPattern) {
		Pattern clone = new Pattern();
		clone.name = nameOfNewPattern;
		clone.numberOfObservations = numberOfObservations;
		for (Feature f : probabilities.keySet()) {
			Double probability = probabilities.get(f);
			clone.probabilities.put(f, probability);
		}
		return clone;
	}

	@Override
	public String toString() {
		String out = "[pattern \"" + name + "\":\n";

		for (Feature f : probabilities.keySet()) {
			String name = f.toString();
			// if (f instanceof ContextFeature) {
			// name = "CTX: " + f.shortName();
			// } else if (f instanceof KindFeature) {
			// name = "KIN: " + f.shortName();
			// } else if (f instanceof DefinitionFeature) {
			// name = "DEF: " + f.shortName();
			// } else if (f instanceof CallFeature) {
			// name = "CALL: " + f.shortName();
			// } else
			// name = "type: " + f.shortName();
			out += "\t" + name + " : " + probabilities.get(f) + "\n";
		}

		return out + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}