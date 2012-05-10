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
 * The class <code>SnippetParserTest</code> contains tests for the class <code>{@link SnippetParser}</code>.
 * 
 * @author Cheng Chen
 */
public class SnippetParserTest {
    private StringBuilder json;

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_1() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(3, result.length);
    }

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_2() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        effect.setCode("new code here");

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(1, result.length);
    }

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_3() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        effect.setCode("code with two parameters ${value1} and ${value2}");

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(4, result.length);
    }

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_4() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        effect.setCode("${just a parameter}");

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(2, result.length);
    }

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_5() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        effect.setCode("for(int i=0; i<${value};i++) System.out.println(${value});");

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(5, result.length);
    }

    /**
     * Run the ISnippetNode[] parseSnippetNodes(Effect) method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testParseSnippetNodes_6() throws Exception {
        Effect effect = GsonUtil.deserialize(json, Effect.class);
        effect.setCode("System.out.println(\"helloworld\")");

        ISnippetNode[] result = SnippetParser.parseSnippetNodes(effect);

        assertNotNull(result);
        assertEquals(1, result.length);
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
        new org.junit.runner.JUnitCore().run(SnippetParserTest.class);
    }
}