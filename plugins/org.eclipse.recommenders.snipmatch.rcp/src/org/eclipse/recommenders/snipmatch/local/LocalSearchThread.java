/**
 * Copyright (c) 2011,2012 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Cheng Chen - initial API and implementation.
 */

package org.eclipse.recommenders.snipmatch.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.preferences.PreferenceConstants;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.recommenders.snipmatch.search.SnipMatchSearchEngine;
import org.eclipse.recommenders.snipmatch.web.ISearchListener;

/**
 * A class for searching local snippets.
 */
public class LocalSearchThread extends Thread {
    protected LocalMatchClient client;
    protected boolean done;

    private int waitTime = 450;
    private MatchEnvironment env;
    private String query;
    private ISearchListener listener;

    public LocalSearchThread(LocalMatchClient client, MatchEnvironment env, String query, ISearchListener listener) {

        this.client = client;
        this.env = env;
        this.query = query;
        this.listener = listener;
        this.done = false;
    }

    @Override
    public void run() {
        SnipMatchSearchEngine searchEngine = SnipMatchPlugin.getDefault().getSearchEngine();

        String snippetsDir = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_STORE_DIR);
        String indexDir = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
        // Everything is prepared, just wait for final query string
        try {
            sleep(waitTime);
        } catch (Exception e) {
            e.printStackTrace();
            listener.searchFailed("Thread exception");
            done = true;
            return;
        }
        if (!searchEngine.isInitialized(snippetsDir, indexDir)) {
            // Not initialed, initial it first
            try {
                searchEngine.initialize(snippetsDir, indexDir);
            } catch (IOException e) {
                e.printStackTrace();
                done = true;
                return;
            }
        }
        if (done)
            return;

        List<MatchNode> resultList = searchEngine.search(query);
        if (done)
            return;

        if (resultList == null)
            listener.searchFailed("No snippets match for " + query);
        else {
            for (MatchNode match : resultList) {
                if (match != null)
                    listener.matchFound(match);
            }
            listener.searchSucceeded();
            done = true;
        }
    }

    public boolean isDone() {

        return done;
    }

    protected InputStream search() {
        if (done)
            return null;

        return null;
    }

    public void cancel() {
        done = true;
    }
}
