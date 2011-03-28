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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//call chain 1 ok
public class CompletionWithCastInMethod {

    public InputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });

    public Collection<String> findMe2 = new ArrayList<String>();

    public static void test_castToSubClass() {
        //@start
		final CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		final ByteArrayInputStream c = <^Space|.*useMe.findMe.*2 elements.*>
		//@end
		//final CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		//final ByteArrayInputStream c = (ByteArrayInputStream) useMe.findMe
	}

    public static void test_castToInterface() {
        //@start
		final CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		final List<String> c = <^Space|.*useMe.findMe.*>
		//@end
		//final CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		//final List<String> c = (List) useMe.findMe2
	}
}
