/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;

@SuppressWarnings("restriction")
public class SnipmatchRcpPreferences {

    private String fetchUrl;
    private String pushUrl;
    private String pushBranch;

    @Inject
    public void setFetchUrl(@Preference(Constants.PREF_SNIPPETS_REPO_FETCH_URL) String newValue) {
        fetchUrl = newValue;
    }

    public String getFetchUrl() {
        return fetchUrl;
    }

    @Inject
    public void setPushUrl(@Preference(Constants.PREF_SNIPPETS_REPO_PUSH_URL) String newValue) {
        pushUrl = newValue;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    @Inject
    public void setPushBranch(@Preference(Constants.PREF_SNIPPETS_REPO_PUSH_BRANCH) String newValue) {
        pushBranch = newValue;
    }

    public String getPushBranch() {
        return pushBranch;
    }
}
