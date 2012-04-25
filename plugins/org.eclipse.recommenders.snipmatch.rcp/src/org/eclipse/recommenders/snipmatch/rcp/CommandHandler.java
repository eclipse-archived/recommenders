/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handles all plugin commands.
 */
public class CommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String id = event.getCommand().getId();

		if (id.equals("snipmatch.eclipse.commands.login")) {
			
			SnipMatchPlugin.getDefault().showLoginBox(null, null);
		}
		else if (id.equals("snipmatch.eclipse.commands.logout")) {
			
			SnipMatchPlugin.getDefault().logout();
		}
		else if (id.equals("snipmatch.eclipse.commands.search")) {

			SnipMatchPlugin.getDefault().showSearchBox("javasnippet");
		}
		else if (id.equals("snipmatch.eclipse.commands.submit")) {

			SnipMatchPlugin.getDefault().showSubmitBox();
		}
		else if (id.equals("snipmatch.eclipse.commands.profile")) {

			SnipMatchPlugin.getDefault().showProfileBox();
		}

		return null;
	}
}
