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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RecommenderOptions {

	// options for feature building

	public double weightContext = 1.0;
	public double weightKind = 1.0;
	public double weightDefinition = 1.0;

	public boolean useContext = true;
	public boolean useKind = false;
	public boolean useDefinition = false;
	public boolean useCallSites = true;
	public boolean useParameterSites = false;

	public boolean useInitAsCall = false;

	public boolean keepRare = false;

	// options for clustering

	public enum Distance {
		MANHATTAN, COSINE, TANIMOTO
	};

	public enum Clusterer {
		COUNTING, CANOPY, KMEANS, COMBINED
	};

	public Distance distance = Distance.MANHATTAN;
	public Clusterer clusterer = Clusterer.COUNTING;

	public double canopyT1 = 0.001;
	public double canopyT2 = 0.001;

	public int kmeansClusterCount = 100;
	public double kmeansDistanceTreshold = 0.01;
	public int kmeansIterations = 3;

	// options for recommending

	public double minPropability = 0.0;

	public String getRecommenderId() {
		String recommenderId = "";

		switch (clusterer) {
		case COUNTING:
			recommenderId = "counting";
			break;
		case CANOPY:
			recommenderId = "canopy[" + canopyT1 + "," + canopyT2 + "]";
			break;
		case KMEANS:
			recommenderId = "kmeans[" + kmeansClusterCount + "," + kmeansIterations + "," + kmeansDistanceTreshold
					+ "]";
			break;
		case COMBINED:
			recommenderId = "combined[" + canopyT1 + "," + canopyT2 + "," + kmeansIterations + ","
					+ kmeansDistanceTreshold + "]";
			break;
		}

		switch (distance) {
		case COSINE:
			recommenderId += "+COSINE";
			break;
		case MANHATTAN:
			recommenderId += "+MANHATTAN";
			break;
		case TANIMOTO:
			recommenderId += "+TANIMOTO";
			break;
		}

		recommenderId += "+W[" + weightContext + "," + weightKind + "," + weightDefinition + "]";
		recommenderId += (keepRare ? '+' : '-') + "RARE";

		recommenderId = recommenderId.replace("canopy[0.1,0.1]+MANHATTAN+W[0.0,0.0,0.0]", "callgroup");

		return recommenderId;
	}

	public String getModifier() {

		String modifier = "";

		modifier += sign(useContext) + "CTX";
		modifier += sign(useKind) + "KIN";
		modifier += sign(useDefinition) + "DEF";
		modifier += sign(useInitAsCall) + "INIT";
		modifier += sign(useCallSites) + "CS";
		modifier += sign(useParameterSites) + "PS";

		if (minPropability > 0.0) {
			modifier += "+MIN" + Math.round(minPropability * 100);
		}

		return modifier;

	}

	public Identifier getIdentifier() {
		return Identifier.create(toString());
	}

	private String sign(boolean value) {
		if (value)
			return "+";
		else
			return "-";
	}

	@Override
	public String toString() {
		return getRecommenderId() + getModifier();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public static RecommenderOptions create(String string) {

		string = string.replace("callgroup", "canopy[0.1,0.1]+MANHATTAN+W[0.0,0.0,0.0]");

		RecommenderOptions options = new RecommenderOptions();

		if (keyExists(string, "W"))
			parseWeights(string, options);

		if (keyExists(string, "CTX"))
			options.useContext = parseExistance(string, "CTX");

		if (keyExists(string, "KIN"))
			options.useKind = parseExistance(string, "KIN");

		if (keyExists(string, "DEF"))
			options.useDefinition = parseExistance(string, "DEF");

		if (keyExists(string, "INIT"))
			options.useInitAsCall = parseExistance(string, "INIT");

		if (keyExists(string, "CS"))
			options.useCallSites = parseExistance(string, "CS");

		if (keyExists(string, "PS"))
			options.useParameterSites = parseExistance(string, "PS");

		if (keyExists(string, "RARE"))
			options.keepRare = parseExistance(string, "RARE");

		if (string.indexOf("canopy") != -1) {
			parseCanopy(string, options);
		} else if (string.indexOf("kmeans") != -1) {
			parseKmeans(string, options);
		} else if (string.indexOf("combined") != -1) {
			parseCombined(string, options);
		} else {
			options.clusterer = Clusterer.COUNTING;
		}

		if (keyExists(string, "COSINE"))
			options.distance = Distance.COSINE;
		else if (keyExists(string, "MANHATTAN"))
			options.distance = Distance.MANHATTAN;
		else if (keyExists(string, "TANIMOTO"))
			options.distance = Distance.TANIMOTO;

		parseMin(string, options);

		return options;
	}

	private static boolean keyExists(String string, String key) {
		if (string.indexOf("+" + key) != -1)
			return true;
		else if (string.indexOf("-" + key) != -1)
			return true;
		else
			return false;
	}

	private static boolean parseExistance(String string, String key) {
		if (string.indexOf("+" + key) != -1)
			return true;
		else
			return false;
	}

	private static void parseWeights(String string, RecommenderOptions options) {

		String regExp = ".*W\\[([0-9.]+),([0-9.]+),([0-9.]+)\\].*";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(string);
		if (m.matches()) {
			Double wCTX = Double.parseDouble(m.group(1));
			Double wKIND = Double.parseDouble(m.group(2));
			Double wDEF = Double.parseDouble(m.group(3));
			options.weightContext = wCTX;
			options.weightKind = wKIND;
			options.weightDefinition = wDEF;
		}
	}

	private static void parseCanopy(String string, RecommenderOptions options) {
		String regExp = "canopy\\[([0-9.]+),([0-9.]+)\\].*";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(string);
		if (m.matches()) {
			Double t1 = Double.parseDouble(m.group(1));
			Double t2 = Double.parseDouble(m.group(2));
			options.canopyT1 = t1;
			options.canopyT2 = t2;
			options.clusterer = Clusterer.CANOPY;
		}
	}

	private static void parseKmeans(String string, RecommenderOptions options) {
		String regExp = "kmeans\\[([0-9]+),([0-9.]+),([0-9.]+)\\].*";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(string);
		if (m.matches()) {
			Integer count = Integer.parseInt(m.group(1));
			Integer iterations = Integer.parseInt(m.group(2));
			Double treshold = Double.parseDouble(m.group(3));
			options.kmeansClusterCount = count;
			options.kmeansIterations = iterations;
			options.kmeansDistanceTreshold = treshold;
			options.clusterer = Clusterer.KMEANS;
		}
	}

	private static void parseCombined(String string, RecommenderOptions options) {
		String regExp = "combined\\[([0-9.]+),([0-9.]+),([0-9]+),([0-9.]+)\\].*";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(string);
		if (m.matches()) {
			Double t1 = Double.parseDouble(m.group(1));
			Double t2 = Double.parseDouble(m.group(2));
			Integer iterations = Integer.parseInt(m.group(3));
			Double treshold = Double.parseDouble(m.group(4));
			options.canopyT1 = t1;
			options.canopyT2 = t2;
			options.kmeansIterations = iterations;
			options.kmeansDistanceTreshold = treshold;
			options.clusterer = Clusterer.COMBINED;
		}
	}

	private static void parseMin(String string, RecommenderOptions options) {
		String regExp = ".*\\+MIN([0-9]+).*";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(string);
		if (m.matches()) {
			Double propability = Double.parseDouble(m.group(1));
			options.minPropability = propability / 100.0;
		}
	}

	public static Set<RecommenderOptions> getAll() {
		Set<RecommenderOptions> options = new LinkedHashSet<RecommenderOptions>();

		String baseline = "canopy[0.1,0.1]+W[0,0,0]";
		// String manhattan = "combined[2.6,1.6,10,0.01]+MANHATTAN+W[0.5,0.5,0.5]";
		String manhattan = "combined[3.1,2.1,10,0.01]+MANHATTAN+W[0.5,0.5,0.5]";
		// String cosine = "combined[0.3,0.15,10,0.01]+COSINE+W[0.25,0.25,0.25]";
		String cosine = "combined[0.45,0.3,10,0.01]+COSINE+W[0.3,0.3,0.3]";
		// String tanimoto = "combined[0.5,0.25,10,0.01]+TANIMOTO+W[0.25,0.25,0.25]";

		//
		// KÖNNEN DIE +RARE rausfliegen??
		//
		options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF+INIT+RARE")); // old recommender
		options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF-INIT+RARE")); // baseline

		options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF")); // zeigen, dass baseline-RARE ok ist
		options.add(RecommenderOptions.create(baseline + "+CTX+KIN-DEF")); // nur mit kind
		options.add(RecommenderOptions.create(baseline + "+CTX-KIN+DEF")); // nur mit def
		options.add(RecommenderOptions.create(baseline + "+CTX+KIN+DEF")); // mit allen def infos

		// T1 has to be greater than T2 !!!!
		options.add(RecommenderOptions.create("combined[3.1,2.1,10,0.01]+MANHATTAN+W[0,0,0]-RARE+CTX-KIN-DEF"));
		options.add(RecommenderOptions.create("combined[3.1,2.1,10,0.01]+MANHATTAN+W[0,0,0]-RARE+CTX+KIN+DEF"));

		// options.add(RecommenderOptions.create(manhattan + "+RARE+CTX-KIN-DEF"));
		// options.add(RecommenderOptions.create(manhattan + "+RARE+CTX+KIN+DEF"));
		options.add(RecommenderOptions.create(manhattan + "+CTX-KIN-DEF"));
		options.add(RecommenderOptions.create(manhattan + "+CTX+KIN-DEF"));
		options.add(RecommenderOptions.create(manhattan + "+CTX-KIN+DEF"));
		options.add(RecommenderOptions.create(manhattan + "+CTX+KIN+DEF"));

		options.add(RecommenderOptions.create(cosine + "+CTX+KIN+DEF"));
		options.add(RecommenderOptions.create(cosine + "+CTX-KIN-DEF"));

		return options;
	}

	// // VORHER:
	//
	// // KÖNNEN DIE +RARE rausfliegen??
	// //
	// options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF+INIT+RARE")); // dirty baseline (patterns ==
	// callgroups)
	// options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF-INIT+RARE")); // baseline (patterns == callgroups)
	// options.add(RecommenderOptions.create(baseline + "+CTX+KIN+DEF-INIT+RARE")); // baseline mit def
	//
	// options.add(RecommenderOptions.create(baseline + "+CTX-KIN-DEF")); // zeigen, dass baseline-RARE ok ist
	// options.add(RecommenderOptions.create(baseline + "+CTX+KIN-DEF")); // nur mit kind
	// options.add(RecommenderOptions.create(baseline + "+CTX-KIN+DEF")); // nur mit def
	// options.add(RecommenderOptions.create(baseline + "+CTX+KIN+DEF")); // mit allen def infos
	//
	// //
	// // UMSTELLEN AUF -RARE?!
	// //
	// options.add(RecommenderOptions.create("combined[1.6,2.6,10,0.01]+MANHATTAN+W[0,0,0]+RARE+CTX-KIN-DEF"));
	// options.add(RecommenderOptions.create("combined[1.6,2.6,10,0.01]+MANHATTAN+W[0,0,0]+RARE+CTX+KIN+DEF"));
	//
	// options.add(RecommenderOptions.create(manhattan + "+RARE+CTX-KIN-DEF"));
	// options.add(RecommenderOptions.create(manhattan + "+RARE+CTX+KIN+DEF"));
	// options.add(RecommenderOptions.create(manhattan + "+CTX-KIN-DEF"));
	// options.add(RecommenderOptions.create(manhattan + "+CTX+KIN-DEF"));
	// options.add(RecommenderOptions.create(manhattan + "+CTX-KIN+DEF"));
	// options.add(RecommenderOptions.create(manhattan + "+CTX+KIN+DEF"));
	//
	// //
	// // ADD A -KIN-DEF OPTION
	// //
	// options.add(RecommenderOptions.create(cosine + "+CTX+KIN+DEF"));
	// options.add(RecommenderOptions.create(tanimoto + "+CTX+KIN+DEF"));

}