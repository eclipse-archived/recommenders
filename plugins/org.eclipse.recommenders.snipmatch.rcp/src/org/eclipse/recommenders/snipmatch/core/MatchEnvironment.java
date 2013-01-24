/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.core;

import java.util.HashMap;

/**
 * This class represents a match "environment", which is responsible for getting code context information and
 * integrating the returned matches. User should extend this class.
 */
public abstract class MatchEnvironment {

    public MatchEnvironment() {
    }

    /**
     * Returns the name of this environment.
     * 
     * @return The name of this environment.
     */
    public abstract String getName();

    /**
     * Returns the friendly name of this environment.
     * 
     * @return The friendly name of this environment.
     */
    public abstract String getFriendlyName();

    /**
     * Returns an array of major types handled by this environment.
     * 
     * @return An array of major types handled by this environment.
     */
    public abstract String[] getMajorTypes();

    /**
     * Returns n array of the friendly names of the major types handled by this environment.
     * 
     * @return An array of the friendly names of the major types handled by this environment.
     */
    public abstract String[] getFriendlyMajorTypes();

    /**
     * Returns token interpretations for a search query.
     * 
     * @param query
     *            The search query.
     * @return A hash map containing the interpretations for each token in the query.
     */
    public HashMap<String, String> getQueryTokenInterpretations(String query) {

        HashMap<String, String> interps = new HashMap<String, String>();
        String[] tokens = query.split(" ");

        for (String token : tokens) {
            interps.put(token, getQueryTokenInterpretation(token));
        }

        return interps;
    }

    /**
     * Tests a match returned by the search to see if it is valid within the current environment.
     * 
     * @param match
     *            The match returned by the search.
     * @return True if the match is valid, false otherwise.
     */
    public abstract boolean testMatch(MatchNode match);

    /**
     * Reset the match environment to its default state. For example, all code changes are rolled back.
     */
    public abstract void reset();

    /**
     * Applies a match to the environment.
     * 
     * @param match
     *            The match to apply.
     * @throws Exception
     */
    public void applyMatch(MatchNode match) throws Exception {

        // First, get the evaluated result of the match.
        Object result = evaluateMatchNode(match, false);
        // Apply the result of the match to the environment.
        if (match instanceof EffectMatchNode) {

            Effect effect = ((EffectMatchNode) match).getEffect();
            applyResult(result, effect);
        } else
            applyResult(result, null);
    }

    /**
     * Returns an array of completions for one argument. Must be implemented by derived classes.
     * 
     * @param argNode
     *            The argument node to complete.
     * @return An array of possible completions for the given argument.
     */
    public abstract String[] getArgumentCompletions(ArgumentMatchNode argNode);

    /**
     * Returns the best possible interpretation of the given token's type, in the current context.
     * 
     * @param token
     *            The token to interpret.
     * @return The full type string of the given token. e.g., expr:java.io.File
     */
    protected abstract String getQueryTokenInterpretation(String token);

    /**
     * Evaluate one match. The evaluated result of the root match node is applied to the code environment.
     * 
     * @param node
     *            The match node to evaluate.
     * @return The result of the evaluation.
     */
    protected Object evaluateMatchNode(MatchNode node, boolean overview) {
        if (node instanceof EffectMatchNode) {

            EffectMatchNode effectNode = (EffectMatchNode) node;

            Object[] args = new Object[effectNode.getEffect().numParameters()];

            for (int i = 0; i < args.length; i++)
                args[i] = (String) evaluateMatchNode(effectNode.getChild(i), overview);

            return evaluateEffect(effectNode, args);
        } else {
            ArgumentMatchNode arg = (ArgumentMatchNode) node;
            if (arg.getParameter().getValue() == null || arg.getParameter().getValue().isEmpty())
                if (overview)
                    return "$" + arg.getArgument();
                else
                    return arg.getArgument();
            else
                return arg.getParameter().getValue();
        }
    }

    /**
     * Evaluates one effect with the given arguments. Must be implemented by derived classes.
     * 
     * @param effectNode
     *            The effect to evaluate.
     * @param args
     *            The arguments for the effect.
     * @return The result of the evaluation.
     */
    protected abstract Object evaluateEffect(EffectMatchNode effectNode, Object[] args);

    /**
     * Apply the evaluated result of a match to the environment.
     * 
     * @param result
     *            The evaluated result of the match.
     * @param effect
     *            The effect of the root match node.
     * @throws Exception
     */
    protected abstract void applyResult(Object result, Effect effect) throws Exception;

}
