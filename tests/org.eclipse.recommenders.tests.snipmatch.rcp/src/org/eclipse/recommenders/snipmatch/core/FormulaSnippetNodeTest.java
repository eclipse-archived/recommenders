/**
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
 * The class <code>FormulaSnippetNodeTest</code> contains tests for the class <code>{@link FormulaSnippetNode}</code>.
 *
 * @author Cheng Chen
 */
public class FormulaSnippetNodeTest {
    private StringBuilder json;
    /**
	 * Run the FormulaSnippetNode(String,String[],Effect) constructor test.
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testFormulaSnippetNode_1()
		throws Exception {
		String name = "name";
		String[] args = new String[] {"args0", "args1"};
		Effect effect = GsonUtil.deserialize(json, Effect.class);

		FormulaSnippetNode result = new FormulaSnippetNode(name, args, effect);

		assertNotNull(result);
		assertEquals(null, result.getNewVariableName());
		assertEquals(2, result.numArguments());
		assertEquals("name", result.getName());
		assertEquals(result.getArgument(0), "args0");
		assertEquals(result.getArgument(1), "args1");

		result.setNewVariableName("newVarName");
		assertEquals(result.getNewVariableName(), "newVarName");
	}

	/**
	 * Run the FormulaSnippetNode(String,String[],Effect) constructor test.
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testFormulaSnippetNode_2()
		throws Exception {
		String name = "print value";
		String[] args = new String[] {"value1", "value2", "value3"};
		Effect effect = new Effect();

		FormulaSnippetNode result = new FormulaSnippetNode(name, args, effect);

		assertNotNull(result);
		assertEquals(null, result.getNewVariableName());
		assertEquals(3, result.numArguments());
		assertEquals("print value", result.getName());
		
		assertEquals(result.getArgument(0), "value1");
        assertEquals(result.getArgument(1), "value2");
        assertEquals(result.getArgument(2), "value3");

        result.setNewVariableName("variable name");
        assertEquals(result.getNewVariableName(), "variable name");
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 */
	@Before
	public void setUp()
		throws Exception {
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
	 *         if the clean-up fails for some reason
	 *
	 */
	@After
	public void tearDown()
		throws Exception {
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(FormulaSnippetNodeTest.class);
	}
}