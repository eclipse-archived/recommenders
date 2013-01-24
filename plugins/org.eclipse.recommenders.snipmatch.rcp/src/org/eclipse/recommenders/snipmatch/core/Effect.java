/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.core;

import java.util.ArrayList;


/**
 * This class represents one snippet. A match can contain many snippets in a nested structure.
 * Nesting is no longer used in the current version of SnipMatch.
 * SnipMatch can theoretically support effects that are not snippets.
 * For example, an effect could be a set of instructions to be executed in some program.
 * However, since this library is only being used for the Eclipse plugin, an effect is essentially a snippet.
 */
public class Effect {
	
	private ArrayList<String> patterns;
	private ArrayList<EffectParameter> params;
	private String envName = "";
	/**
	 * The major type is used by the server to categorize and nest results.
	 * Examples of major types are "expr" (expression) and "stmt" (statement).
	 */
	private String majorType = "";
	/**
	 * The minor type is used by the client to further check snippet compatibility in nested results.
	 * Examples of minor types are "int" and "java.util.ArrayList".
	 */
	private String minorType = "";
	private String code = "";
	private String summary = "";
	private String id = "";
	
	public Effect() {

		patterns = new ArrayList<String>();
		params = new ArrayList<EffectParameter>();
	}
	
	
	/**
	 * Adds a search pattern to the effect.
	 * @param pattern The pattern to add.
	 */
	public void addPattern(String pattern) {
		
		patterns.add(pattern);
	}
	
	/**
	 * Returns a search pattern by index.
	 * @param index The pattern index.
	 * @return The search pattern at the given index.
	 */
	public String getPattern(int index) {
		
		return patterns.get(index);
	}
	
	/**
	 * Returns an array containing all the search patterns for this effect.
	 * @return An array containing all the search patterns for this effect.
	 */
	public String[] getPatterns() {
		
		return patterns.toArray(new String[patterns.size()]);
	}
	
	/**
	 * Sets the search pattern at the given index.
	 * @param index The pattern index.
	 * @param pattern The search pattern.
	 */
	public void setPattern(int index, String pattern) {
		
		patterns.set(index, pattern);
	}
	
	/**
	 * Removes the search pattern at the given index.
	 * @param index The pattern index.
	 */
	public void removePattern(int index) {
		
		patterns.remove(index);
	}
	
	/**
	 * Removes all search patterns from this effect.
	 */
	public void clearPatterns() {
		
		patterns.clear();
	}
	
	/**
	 * Returns the number of search patterns associated with this effect.
	 * @return The number of search patterns associated with this effect.
	 */
	public int numPatterns() {
		
		return patterns.size();
	}
	
	/**
	 * Adds a new search parameter to this effect.
	 * @param param The new search parameter.
	 */
	public void addParameter(EffectParameter param) {
		
		params.add(param);
	}
	
	/**
	 * Returns a search parameter by index.
	 * @param index The parameter index.
	 * @return The search parameter at the given index.
	 */
	public EffectParameter getParameter(int index) {
		
		return params.get(index);
	}
	
	/**
	 * Returns a search parameter by name.
	 * @param name The name of the search parameter.
	 * @return The search parameter with the given name. Returns null if no such parameter exists.
	 */
	public EffectParameter getParameter(String name) {

		for (EffectParameter param : params) {
			if (param.getName().equals(name)) return param;
		}
		
		return null;
	}
	
	/**
	 * Returns an array containing all the search parameters of this effect.
	 * @return An array containing all the search parameters of this effect.
	 */
	public EffectParameter[] getParameters() {
		
		return params.toArray(new EffectParameter[params.size()]);
	}
	
	/**
	 * Removes a search parameter by index.
	 * @param index The parameter index.
	 */
	public void removeParameter(int index) {
		
		params.remove(index);
	}
	
	/**
	 * Removes a search parameter by name.
	 * @param name The name of the search parameter to remove.
	 */
	public void removeParameter(String name) {

		int index = getParameterIndex(name);
		if (index != -1) removeParameter(index);
	}
	
	/**
	 * Returns the index of the search parameter with the given name.
	 * @param name The name of the search parameter.
	 * @return The index of the search parameter.
	 */
	public int getParameterIndex(String name) {

		for (int i = 0; i < params.size(); i++) {
			if (params.get(i).getName().equals(name)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Removes all search parameters in this effect.
	 */
	public void clearParameters() {
		
		params.clear();
	}
	
	/**
	 * Returns the number of search parameters in this effect.
	 * @return The number of search parameters in this effect.
	 */
	public int numParameters() {
		
		return params.size();
	}

	/**
	 * Sets the code contents of this effect.
	 * @param code The code contents.
	 */
	public void setCode(String code) {

		this.code = code;
	}

	/**
	 * Returns the code contents of this effect.
	 * @return The code contents of this effect.
	 */
	public String getCode() {

		return code;
	}

	/**
	 * Sets the effect summary string.
	 * @param summary The effect summary string.
	 */
	public void setSummary(String summary) {
		
		this.summary = summary;
	}

	/**
	 * Returns the effect summary string.
	 * @return The effect summary string.
	 */
	public String getSummary() {
		
		return summary;
	}
	
	/**
	 * Sets the major type of this effect.
	 * @param majorType The major type.
	 */
	public void setMajorType(String majorType) {
		
		this.majorType = majorType;
	}

	/**
	 * Returns the major type of this effect.
	 * @return The major type of this effect.
	 */
	public String getMajorType() {

		return majorType;
	}
	
	/**
	 * Sets the minor type of this effect.
	 * @param minorType The minor type.
	 */
	public void setMinorType(String minorType) {
		
		this.minorType = minorType;
	}

	/**
	 * Returns the minor type of this effect.
	 * @return The minor type of this effect.
	 */
	public String getMinorType() {

		return minorType;
	}

	/**
	 * Returns the full type string for this effect, which is just <majorType>:<minorType>
	 * @return The full type string for this effect.
	 */
	public String getFullType() {
		
		if (!minorType.isEmpty())
			return majorType + ":" + minorType;
		else return majorType;
	}

	/**
	 * Sets the environment name for this effect.
	 * @param envName The environment name.
	 */
	public void setEnvironmentName(String envName) {
		
		this.envName = envName;
	}

	/**
	 * Returns the environment name for this effect.
	 * @return The environment name for this effect.
	 */
	public String getEnvironmentName() {

		return envName;
	}

	/**
	 * Sets the ID of this effect.
	 * @param id The ID.
	 */
	public void setId(String id) {
		
		this.id = id;
	}

	/**
	 * Returns the effect's ID.
	 * @return The effect's ID.
	 */
	public String getId() {

		return id;
	}
}
