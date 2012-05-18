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

import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>ArgumentMatchNodeTest</code> contains tests for the class <code>{@link ArgumentMatchNode}</code>.
 * 
 * @author Cheng Chen
 */
public class ArgumentMatchNodeTest {
    /**
     * Run the ArgumentMatchNode(String,EffectParameter) constructor test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testArgumentMatchNode_1() throws Exception {
        String arg = "obj";
        EffectParameter param = new EffectParameter();
        param.setMajorType("major");
        param.setMinorType("minor");
        param.setName("name");

        ArgumentMatchNode result = new ArgumentMatchNode(arg, param);

        assertNotNull(result);
        assertEquals("obj", result.getArgument());
        assertEquals(null, result.getMatchType());
        assertEquals(null, result.getParent());
        assertEquals(result.getParameter().getFullType(), "major:minor");
        result.setMatchType(MatchType.EXACT_MATCH);
        assertEquals(MatchType.EXACT_MATCH, result.getMatchType());
    }

    /**
     * Run the MatchNode clone() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testClone_1() throws Exception {
        ArgumentMatchNode fixture = new ArgumentMatchNode("obj", new EffectParameter());
        fixture.parent = new ArgumentMatchNode("expr", new EffectParameter());
        fixture.matchType = MatchType.EXACT_MATCH;

        MatchNode result = fixture.clone();

        assertNotNull(result);
        assertEquals(null, result.getParent());
        assertEquals(result.getMatchType(), MatchType.EXACT_MATCH);
    }

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testEquals_1() throws Exception {
        ArgumentMatchNode fixture = new ArgumentMatchNode("match", new EffectParameter());
        fixture.parent = new ArgumentMatchNode("parent", new EffectParameter());
        fixture.matchType = MatchType.EXACT_MATCH;
        Object other = new ArgumentMatchNode("arg", new EffectParameter());

        boolean result = fixture.equals(other);

        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testEquals_2() throws Exception {
        ArgumentMatchNode fixture = new ArgumentMatchNode("arg", new EffectParameter());
        fixture.parent = new ArgumentMatchNode("parent", new EffectParameter());
        fixture.matchType = MatchType.EXACT_MATCH;
        Object other = new ArgumentMatchNode("arg", new EffectParameter());

        boolean result = fixture.equals(other);

        assertEquals(true, result);
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
        new org.junit.runner.JUnitCore().run(ArgumentMatchNodeTest.class);
    }
}