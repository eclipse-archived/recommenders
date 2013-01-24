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
 * The class <code>TextSnippetNodeTest</code> contains tests for the class <code>{@link TextSnippetNode}</code>.
 * 
 * @author Cheng Chen
 */
public class TextSnippetNodeTest {
    private StringBuilder json;

    /**
     * Run the TextSnippetNode(String,Effect) constructor test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testTextSnippetNode_1() throws Exception {
        String text = "text summary";
        Effect effect = new Effect();

        TextSnippetNode result = new TextSnippetNode(text, effect);

        assertNotNull(result);
        assertEquals("text summary", result.getText());
    }

    /**
     * Run the Effect getEffect() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetEffect_1() throws Exception {
        TextSnippetNode fixture = new TextSnippetNode("", (Effect) GsonUtil.deserialize(json, Effect.class));

        Effect result = fixture.getEffect();

        assertNotNull(result);

        assertEquals(1, result.numParameters());
        assertEquals("Print to standard output.", result.getSummary());
        assertEquals("", result.getMinorType());
        assertEquals("stmt", result.getFullType());
        assertEquals(1, result.numPatterns());
        assertEquals("stmt", result.getMajorType());
        assertEquals("javasnippet", result.getEnvironmentName());
        assertEquals("", result.getId());
        assertEquals("System.out.print(${obj});", result.getCode());
    }

    /**
     * Run the String getText() method test.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testGetText_1() throws Exception {
        TextSnippetNode fixture = new TextSnippetNode("new text", new Effect());

        String result = fixture.getText();

        assertEquals("new text", result);
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
        new org.junit.runner.JUnitCore().run(TextSnippetNodeTest.class);
    }
}
