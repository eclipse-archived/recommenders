/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import helper.FieldsWithDifferentVisibilities;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//call chain 1 ok
public class CompletionOnNonPublicMembersOfSuperClassInMethod extends FieldsWithDifferentVisibilities {

    public static CompletionOnNonPublicMembersOfSuperClassInMethod useMe = new CompletionOnNonPublicMembersOfSuperClassInMethod();

    public static void test_protected() {
		final AtomicBoolean c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe1
		 * variable name --> c
		 */
	}

    public static void test_default() {
		final AtomicInteger c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * NOT expected completion --> useMe.findMe2
		 * variable name --> c
		 */
	}

    public static void test_private() {
		final AtomicLong c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicLong
         * NOT expected completion --> useMe.findMe3
		 * variable name --> c
		 */
	}
}
