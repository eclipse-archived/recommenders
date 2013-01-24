/**
 * Copyright (c) 2011,2012 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Wightman Zi Ye - initial API and implementation.
 *    Cheng Chen - change for local search mode SnipMatch
 */

package org.eclipse.recommenders.snipmatch.local;

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.search.SearchClient;
import org.eclipse.recommenders.snipmatch.web.IDeleteEffectListener;
import org.eclipse.recommenders.snipmatch.web.ILoadProfileListener;
import org.eclipse.recommenders.snipmatch.web.ILoginListener;
import org.eclipse.recommenders.snipmatch.web.ISearchListener;
import org.eclipse.recommenders.snipmatch.web.ISendFeedbackListener;
import org.eclipse.recommenders.snipmatch.web.ISubmitEffectListener;

/**
 * This class is used to search local snippets store.
 */
public final class LocalMatchClient implements SearchClient {
    private String name;
    private String version;
    private String username;
    private String password;
    private LocalSearchThread workThread;

    private static LocalMatchClient instance = new LocalMatchClient();

    private LocalMatchClient() {

    }

    public static LocalMatchClient getInstance() {
        return instance;
    }

    @Override
    public void startSearch(String query, MatchEnvironment env, ISearchListener listener) {
        workThread = new LocalSearchThread(this, env, query, listener);
        workThread.start();
    }

    @Override
    public void cancelWork() {

        if (isWorking())
            workThread.cancel();
    }

    @Override
    public boolean isWorking() {

        return workThread != null && !workThread.isDone();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void startSendFeedback(String query, MatchNode result, String comment, int rating, boolean flag,
            boolean isLog, boolean isStartup, long clientId, boolean used, ISendFeedbackListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isLoggedIn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startProcessing() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopProcessing() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setServerProcessingTime(float serverProcessingTime) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getServerProcessingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getProcessingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void startLogin(String username, String password, ILoginListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void startDeleteEffect(Effect effect, IDeleteEffectListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void startSubmitEffect(Effect effect, boolean isPublic, ISubmitEffectListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void startLoadProfile(ILoadProfileListener listener) {
        // TODO Auto-generated method stub

    }
}
