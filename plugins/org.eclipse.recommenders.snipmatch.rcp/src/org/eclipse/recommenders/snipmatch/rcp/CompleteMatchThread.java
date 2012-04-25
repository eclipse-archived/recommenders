/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;

/**
 * Generates completions for a match asynchronously.
 */
public class CompleteMatchThread implements Runnable {

	boolean canceled;
	MatchEnvironment env;
	MatchNode match;
	ICompleteMatchListener listener;
	
	/**
	 * @param env The match environment.
	 * @param match The match to complete.
	 */
	public CompleteMatchThread(MatchEnvironment env, MatchNode match) {

		this.env = env;
		this.match = match;
		canceled = false;
	}
	
	public void cancel() {
		
		canceled = true;
	}
	
	public void setListener(ICompleteMatchListener listener) {
		
		this.listener = listener;
	}

	@Override
	public void run() {

		// First, test to see if the match is valid.
		if (env.testMatch(match)) {

			// Generate completions for the match, and notify the caller via the listener.
			for (MatchNode completeMatch : env.getMatchCompletions(match)) {

				if (canceled) break;
				if (listener != null) listener.completionFound(completeMatch);
			}
		}
		
		// Tell the caller that we are done.
		if (listener != null) listener.completionFinished();
	}
}
