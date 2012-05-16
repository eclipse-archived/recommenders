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
     * Run the EffectParameter getParameter(String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetParameter_4() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        EffectParameter param = new EffectParameter();
        param.setName("param_name");
        param.setMajorType("major");
        param.setMinorType("minor");
        fixture.addParameter(param);

        EffectParameter result = fixture.getParameter("param_name");

        assertNotNull(result);
        assertEquals("minor", result.getMinorType());
        assertEquals("major:minor", result.getFullType());
        assertEquals("major", result.getMajorType());
        assertEquals("param_name", result.getName());
    }

 /**
     * Run the int getParameterIndex(String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetParameterIndex_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        int result = fixture.getParameterIndex("param_name");
        assertEquals(-1, result);

        EffectParameter param = new EffectParameter();
        param.setName("param_name");
        param.setMajorType("major");
        param.setMinorType("minor");
        fixture.addParameter(param);

        result = fixture.getParameterIndex("param_name");
        assertEquals(1, result);
    }

    /**
     * Run the EffectParameter[] getParameters() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetParameters_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        EffectParameter param = new EffectParameter();
        param.setName("param_name");
        param.setMajorType("major");
        param.setMinorType("minor");
        fixture.addParameter(param);

        EffectParameter[] result = fixture.getParameters();

        assertNotNull(result);
        assertEquals(2, result.length);
        assertNotNull(result[0]);
        assertEquals("", result[0].getMinorType());
        assertEquals("expr", result[0].getFullType());
        assertEquals("expr", result[0].getMajorType());
        assertEquals("obj", result[0].getName());
    }

    /**
     * Run the String getPattern(int) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetPattern_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        int index = 0;
        String result = fixture.getPattern(index);

        assertNotNull(result);
        assertEquals("print $obj", result);
    }

    /**
     * Run the String[] getPatterns() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetPatterns_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);

        String[] result = fixture.getPatterns();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("print $obj", result[0]);
    }

    /**
     * Run the int numParameters() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testNumParameters_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        int result = fixture.numParameters();
        assertEquals(1, result);
    }

    /**
     * Run the int numPatterns() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testNumPatterns_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);

        int result = fixture.numPatterns();

        assertEquals(1, result);
    }

    /**
     * Run the void removeParameter(int) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testRemoveParameter_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        int result = fixture.numParameters();
        assertEquals(1, result);
        fixture.removeParameter(0);
        result = fixture.numParameters();
        assertEquals(0, result);
    }

    /**
     * Run the void removeParameter(String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testRemoveParameter_2() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        int result = fixture.numParameters();
        assertEquals(1, result);
        fixture.removeParameter("obj");
        result = fixture.numParameters();
        assertEquals(0, result);
    }

    /**
     * Run the void removePattern(int) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testRemovePattern_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        assertEquals(1, fixture.numPatterns());
        int index = 0;
        fixture.removePattern(index);
        assertEquals(0, fixture.numPatterns());
    }

    /**
     * Run the void setCode(String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testSetCode_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        String code = "new code";

        fixture.setCode(code);

        assertEquals("new code", fixture.getCode());
    }

    /**
     * Run the void setPattern(int,String) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testSetPattern_1() throws Exception {
        Effect fixture = GsonUtil.deserialize(json, Effect.class);
        fixture.setPattern(0, "new pattern");

        assertEquals("new pattern", fixture.getPattern(0));
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