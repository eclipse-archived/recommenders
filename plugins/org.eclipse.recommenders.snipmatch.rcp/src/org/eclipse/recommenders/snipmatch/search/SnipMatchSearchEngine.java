/**
 * Copyright (c) 2012 Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Cheng Chen - initial API and implementation and/or initial documentation
*/
package org.eclipse.recommenders.snipmatch.search;

import java.io.IOException;
import java.util.List;

import org.eclipse.recommenders.snipmatch.core.MatchNode;

public interface SnipMatchSearchEngine {
	
	//Confirm whether current search engine is initialized by current snippetsDir and indexDir
	public boolean isInitialized(String snippetsDir, String indexDir);
	
	//Initialize search engine
	public void initialize(String snippetsDir, String indexDir) throws IOException;
	
	public void updateIndex();
	
	//Snippets search entrance, return match node list
	public List<MatchNode> search(final String query);

}
