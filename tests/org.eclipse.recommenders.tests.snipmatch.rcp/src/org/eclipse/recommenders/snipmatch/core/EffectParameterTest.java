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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EffectParameterTest</code> contains tests for the class <code>{@link EffectParameter}</code>.
 * 
 * @author Cheng Chen
 */
public class EffectParameterTest {

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     */
    @Test
    public void testEquals_1() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("minor");
        fixture.setMajorType("major");
        fixture.setName("name");
        EffectParameter other = new EffectParameter();
        other.setMinorType("minor");
        other.setMajorType("major");
        other.setName("");

        boolean result = fixture.equals(other);

        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     */
    @Test
    public void testEquals_2() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("minor");
        fixture.setMajorType("major");
        fixture.setName("name");
        EffectParameter other = new EffectParameter();
        other.setName("name");

        boolean result = fixture.equals(other);

        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     */
    @Test
    public void testEquals_3() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("minor");
        fixture.setMajorType("major");
        fixture.setName("name");
        EffectParameter other = new EffectParameter();
        other.setMajorType("major");
        other.setName("name");

        boolean result = fixture.equals(other);

        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     * 
     * @throws Exception
     */
    @Test
    public void testEquals_4() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("minor");
        fixture.setMajorType("major");
        fixture.setName("name");
        EffectParameter other = new EffectParameter();
        other.setMinorType("minor");
        other.setMajorType("major");
        other.setName("name");

        boolean result = fixture.equals(other);

        assertEquals(true, result);
    }

    /**
     * Run the String getFullType() method test.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFullType_1() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("min");
        fixture.setMajorType("maj");
        fixture.setName("name");

        String result = fixture.getFullType();

        assertEquals("maj:min", result);
    }

    /**
     * Run the String getFullType() method test.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFullType_2() throws Exception {
        EffectParameter fixture = new EffectParameter();
        fixture.setMinorType("");
        fixture.setMajorType("maj");
        fixture.setName("name");

        String result = fixture.getFullType();

        assertEquals("maj", result);
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
        new org.junit.runner.JUnitCore().run(EffectParameterTest.class);
    }
}