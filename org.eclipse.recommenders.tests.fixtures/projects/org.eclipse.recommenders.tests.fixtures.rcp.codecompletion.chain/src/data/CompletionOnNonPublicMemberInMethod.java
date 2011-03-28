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

//call chain 1 ok
public class CompletionOnNonPublicMemberInMethod {

    protected AtomicBoolean findMe1 = new AtomicBoolean();

    AtomicInteger findMe2 = new AtomicInteger();

    private final AtomicLong findMe3 = new AtomicLong();

    public static void test_protected() {
        //@start
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicBoolean c = <^Space|useMe.findMe.*>
		//@end
		//final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		//final AtomicBoolean c = useMe.findMe1
	}

    public static void test_default() {
        //@start
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicInteger c = <^Space|useMe.findMe.*>
		//@end
		//final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		//final AtomicInteger c = useMe.findMe2
	}

    public static void test_private() {
        //@start
		final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		final AtomicLong c = <^Space|useMe.findMe.*>
		//@end
		//final CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		//final AtomicLong c = useMe.findMe3
	}
}
