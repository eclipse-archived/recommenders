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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionOnNonPublicMemberInMethod {

    protected AtomicBoolean findMe1 = new AtomicBoolean();

    AtomicInteger findMe2 = new AtomicInteger();

    private final AtomicLong findMe3 = new AtomicLong();

    public static void test_protected() {
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicBoolean c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe1
		 * variable name --> c
		 */
	}

    public static void test_default() {
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicInteger c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * expected completion --> useMe.findMe2
		 * variable name --> c
		 */
	}

    public static void test_private() {
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicLong c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicLong
         * expected completion --> useMe.findMe3
		 * variable name --> c
		 */
	}
}
