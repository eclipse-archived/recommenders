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

public class CompletionOnSupertypeInMethod {

    public ByteArrayInputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });

    public static void method() {
		final CompletionOnSupertypeInMethod useMe = new CompletionOnSupertypeInMethod();
		final InputStream c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> InputStream
         * expected completion --> useMe.findMe
		 * variable name --> c
		 */
	}
}
