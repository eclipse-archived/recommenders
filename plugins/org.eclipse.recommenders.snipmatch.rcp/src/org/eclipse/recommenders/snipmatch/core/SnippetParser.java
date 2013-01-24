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
 * A utility class with static methods to parse snippets.
 */
public class SnippetParser {

    /**
     * Parses an effect's code into an array of snippet nodes.
     * 
     * @param effect
     *            The effect whose code to parse.
     * @return An array of snippet nodes.
     */
    public static ISnippetNode[] parseSnippetNodes(Effect effect) {

        ArrayList<ISnippetNode> nodes = new ArrayList<ISnippetNode>();

        StringBuilder sb = new StringBuilder();
        boolean inFormula = false;
        boolean hitDollar = false;

        for (char c : effect.getCode().toCharArray()) {

            if (inFormula) {

                switch (c) {

                case '}':

                    ISnippetNode node;

                    try {
                        node = parseFormulaSnippetNode(sb.toString(), effect);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }

                    nodes.add(node);
                    inFormula = false;
                    sb = new StringBuilder();

                    break;

                default:

                    sb.append(c);
                }
            } else {

                switch (c) {

                case '$':

                    if (hitDollar)
                        sb.append('$');
                    hitDollar = !hitDollar;
                    break;

                case '{':

                    if (hitDollar) {

                        nodes.add(new TextSnippetNode(sb.toString(), effect));
                        inFormula = true;
                        hitDollar = false;
                        sb = new StringBuilder();
                    } else
                        sb.append('{');

                    break;

                default:

                    sb.append(c);
                }
            }
        }

        if (sb.length() != 0) {
            nodes.add(new TextSnippetNode(sb.toString(), effect));
        }

        return nodes.toArray(new ISnippetNode[nodes.size()]);
    }

    /**
     * Parses one formula snippet node.
     * 
     * @param code
     *            The segment of code to parse for the formula.
     * @param effect
     *            The effect containing the code.
     * @return A formula snippet node.
     */
    private static FormulaSnippetNode parseFormulaSnippetNode(String code, Effect effect) {

        assert code.matches("\\A(\\w+:)?\\w+(\\((\\w+,)*\\w+\\))?\\Z");

        int openPar = code.indexOf('(');
        int closePar = code.indexOf(')');
        int colon = code.indexOf(':');

        String outerCode;
        String innerCode;
        String name;
        String newVarName = null;
        String[] args = null;

        if (openPar != -1) {

            outerCode = code.substring(0, openPar);
            innerCode = code.substring(openPar + 1, closePar);
            args = innerCode.split(",");
        } else
            outerCode = code;

        if (colon != -1) {

            name = outerCode.substring(colon + 1);
            newVarName = outerCode.substring(0, colon);
        } else
            name = outerCode;
        FormulaSnippetNode node = new FormulaSnippetNode(name, args, effect);
        if (newVarName != null)
            node.setNewVariableName(newVarName);
        return node;
    }
}
