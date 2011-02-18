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

public class CompletionOnSupertypeMemberInMethod {

    public static class Subtype extends CompletionOnSupertypeMemberInMethod {
    }

    public AtomicBoolean findMe = new AtomicBoolean();

    public AtomicInteger findMe() {
        return new AtomicInteger();
    }

    public static void test_onAttribute() {
		final Subtype useMe = new Subtype();
		final AtomicBoolean c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe
		 * variable name --> c
		 */
	}

    public static void test_onMethod() {
		final Subtype useMe = new Subtype();
		final AtomicInteger c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * expected completion --> useMe.findMe()
		 * variable name --> c
		 */
	}
}
