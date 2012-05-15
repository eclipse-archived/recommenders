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
 * The class <code>EffectTest</code> contains tests for the class <code>{@link Effect}</code>.
 * 
 * @author Cheng Chen
 */
public class EffectTest {
    private StringBuilder json;
    private String newLine = "\n";

    @Test
    public void testEffect_1() {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        assertEquals(effect.getCode(), "System.out.print(${obj});");
        assertEquals(effect.getEnvironmentName(), "javasnippet");
        assertEquals(effect.getFullType(), "stmt");
        assertEquals(effect.getMajorType(), "stmt");
        assertEquals(effect.getMinorType(), "");
        assertEquals(effect.getSummary(), "Print to standard output.");
        assertEquals(effect.getParameters().length, 1);
        assertEquals(effect.getParameter(0).getName(), "obj");
        assertEquals(effect.getParameter(0).getFullType(), "expr");
        assertEquals(effect.getParameter(0).getMajorType(), "expr");
        assertEquals(effect.getParameter(0).getMinorType(), "");
    }

    /**
     * Run the Effect() constructor test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testEffect_2() throws Exception {
        Effect result = new Effect();
        result.setSummary("summary");
        result.setEnvironmentName("java");
        result.setCode("while(true)");
        assertNotNull(result);

        assertEquals(0, result.numParameters());
        assertEquals("summary", result.getSummary());
        assertEquals("", result.getMinorType());
        assertEquals("", result.getFullType());
        assertEquals(0, result.numPatterns());
        assertEquals("", result.getMajorType());
        assertEquals("java", result.getEnvironmentName());
        assertEquals("", result.getId());
        assertEquals("while(true)", result.getCode());

        String json = GsonUtil.serialize(result);
        assertNotNull(json);

        StringBuilder target = new StringBuilder();
        target.append("{" + newLine);
        target.append("  \"patterns\": []," + newLine);
        target.append("  \"params\": []," + newLine);
        target.append("  \"envName\": \"java\"," + newLine);
        target.append("  \"majorType\": \"\"," + newLine);
        target.append("  \"minorType\": \"\"," + newLine);
        target.append("  \"code\": \"while(true)\"," + newLine);
        target.append("  \"summary\": \"summary\"," + newLine);
        target.append("  \"id\": \"\"" + newLine);
        target.append("}");
        assertEquals(json, target.toString());
    }

    /**
     * Run the void addParameter(EffectParameter) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testAddParameter_1() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        EffectParameter param = new EffectParameter();
        param.setMajorType("expr");
        param.setName("env");

        assertEquals(effect.getParameters().length, 1);
        effect.addParameter(param);
        assertEquals(effect.getParameters().length, 2);
    }

    /**
     * Run the void addPattern(String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testAddPattern_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        String pattern = "new pattern";

        assertEquals(fixture.getPatterns().length, 1);
        fixture.addPattern(pattern);
        assertEquals(fixture.getPatterns().length, 2);
    }

    /**
     * Run the void clearParameters() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testClearParameters_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        assertEquals(fixture.getParameters().length, 1);
        fixture.clearParameters();
        assertEquals(fixture.getParameters().length, 0);
    }

    /**
     * Run the void clearPatterns() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testClearPatterns_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        assertEquals(fixture.getPatterns().length, 1);
        fixture.clearPatterns();
        assertEquals(fixture.getPatterns().length, 0);
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
        json.append("	{");
        json.append("	\"patterns\": [");
        json.append("		\"print $obj\"");
        json.append("	],");
        json.append("	\"params\": [");
        json.append("	{");
        json.append("		\"name\": \"obj\",");
        json.append("		\"majorType\": \"expr\",");
        json.append("		\"minorType\": \"\"");
        json.append("	}");
        json.append("	],");
        json.append("	\"envName\": \"javasnippet\",");
        json.append("	\"majorType\": \"stmt\",");
        json.append("	\"minorType\": \"\",");
        json.append("	\"code\": \"System.out.print(${obj});\",");
        json.append("	\"summary\": \"Print to standard output.\",");
        json.append("	\"id\": \"\"");
        json.append("	}");
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
        // Add additional tear down code here
    }

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     * 
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(EffectTest.class);
    }
}