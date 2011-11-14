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
        //@start
		final AtomicBoolean c = <^Space|useMe.findMe.*>
		//@end
		//final AtomicBoolean c = useMe.findMe1
	}

    public static void test_default() {
        //@start
		final AtomicInteger c = <^Space|useMe.*>
		//@end
		//final AtomicInteger c = useMe
		
		/*
         * NOT expected completion --> useMe.findMe2
		 */
	}

    public static void test_private() {
        //@start
		final AtomicLong c = <^Space|useMe.*>
		//@end
		//final AtomicLong c = useMe
		
		/*
         * NOT expected completion --> useMe.findMe3
		 */
	}
}
