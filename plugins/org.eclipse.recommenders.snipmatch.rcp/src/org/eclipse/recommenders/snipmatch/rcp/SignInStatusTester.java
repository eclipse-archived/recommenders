/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.core.expressions.PropertyTester;

/**
 * This is to test for the login status of the client, so the plugin menu can reflect it.
 */
public class SignInStatusTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		
		return SnipMatchPlugin.isLoggedIn();
	}
}
