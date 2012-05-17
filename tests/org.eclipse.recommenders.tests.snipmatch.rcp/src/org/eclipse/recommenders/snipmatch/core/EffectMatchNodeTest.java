/**
 * Copyright (c) 2011, University of Science and Technology Beijing
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *      Cheng Chen - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch.core;

import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>EffectMatchNodeTest</code> contains tests for the class <code>{@link EffectMatchNode}</code>.
 * 
 * @author Cheng Chen
 */
public class EffectMatchNodeTest {
    private StringBuilder json;
    private String newLine = "\n";

    /**
     * Run the EffectMatchNode(Effect,String,MatchNode[]) constructor test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testEffectMatchNode_1() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        String pattern = "print $obj";
        MatchNode[] children = new MatchNode[effect.getParameters().length];
        for (int i = 0; i < children.length; i++) {
            EffectParameter param = effect.getParameters()[i];
            ArgumentMatchNode childNode = new ArgumentMatchNode(param.getName(), param);
            children[i] = childNode;
        }

        EffectMatchNode result = new EffectMatchNode(effect, pattern, children);

        assertNotNull(result);
        assertEquals(true, result.isComplete());
        assertEquals(1, result.numChildren());
        assertEquals(false, result.isEmpty());
        assertEquals("print $obj", result.getPattern());
        assertEquals(null, result.getMatchType());
        assertEquals(null, result.getParent());

        MatchNode[] nodes = result.getChildren();
        assertEquals(nodes.length, 1);
    }

    /**
     * Perform pre-test initialization.
     * 
     * @throws Exception
     *             if the initialization fails for some reason
     * 
     */
    @Before
    public void setUp() throws Exception {
        json = new StringBuilder();
        json.append("   {");
        json.append("   \"patterns\": [");
        json.append("       \"print $obj\"");
        json.append("   ],");
        json.append("   \"params\": [");
        json.append("   {");
        json.append("       \"name\": \"obj\",");
        json.append("       \"majorType\": \"expr\",");
        json.append("       \"minorType\": \"\"");
        json.append("   }");
        json.append("   ],");
        json.append("   \"envName\": \"javasnippet\",");
        json.append("   \"majorType\": \"stmt\",");
        json.append("   \"minorType\": \"\",");
        json.append("   \"code\": \"System.out.print(${obj});\",");
        json.append("   \"summary\": \"Print to standard output.\",");
        json.append("   \"id\": \"\"");
        json.append("   }");
    }

    /**
     * Perform post-test clean-up.
     * 
     * @throws Exception
     *             if the clean-up fails for some reason
     * 
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     * 
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(EffectMatchNodeTest.class);
    }
}