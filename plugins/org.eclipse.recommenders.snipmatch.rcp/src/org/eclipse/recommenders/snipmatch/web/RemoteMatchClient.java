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

package org.eclipse.recommenders.snipmatch.web;

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.recommenders.snipmatch.search.SearchClient;

/**
 * This class is used to communicate with the SnipMatch server.
 */
public final class RemoteMatchClient implements SearchClient {

    /**
     * This listener saves the username and password upon successfully logging in.
     */
    private class InternalLoginListener implements ILoginListener {

        String attemptedUsername;
        String attemptedPassword;

        public InternalLoginListener(String attemptedUsername, String attemptedPassword) {

            this.attemptedUsername = attemptedUsername;
            this.attemptedPassword = attemptedPassword;
        }

        @Override
        public void loginFailed(String error) {

            username = null;
            password = null;
        }

        @Override
        public void loginSucceeded() {

            username = attemptedUsername;
            password = attemptedPassword;
        }
    }

    public static final String HOST_URL = "http://snipmatch.com/";
    public static final String LOGIN_URL = HOST_URL + "Login.php";
    public static final String SEARCH_URL = HOST_URL + "Search.php";
    public static final String SUBMIT_URL = HOST_URL + "InsertEffect.php";
    public static final String FEEDBACK_URL = HOST_URL + "InsertFlag.php";
    public static final String PROFILE_URL = HOST_URL + "RetrieveUserEffects.php";
    public static final String DELETE_URL = HOST_URL + "DeletePattern.php";
    public static final long TIMEOUT = 250;

    private String name;
    private String version;
    private String username;
    private String password;
    private PostThread workThread;
    private long lastPostTime;
    private long processingStartTime;
    private long processingStopTime;
    private float serverProcessingTime;

    private static RemoteMatchClient instance = new RemoteMatchClient("eclipse", SnipMatchPlugin.getDefault()
            .getBundle().getVersion().toString());

    public static RemoteMatchClient getInstance() {
        return instance;
    }

    private RemoteMatchClient(String name, String version) {
        this.name = name;
        this.version = version;
        lastPostTime = 0;
    }

    public String getName() {

        return name;
    }

    public String getVersion() {

        return version;
    }

    public void logout() {

        this.username = null;
        this.password = null;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public boolean isLoggedIn() {

        return username != null;
    }

    @Override
    public boolean isWorking() {

        return workThread != null && !workThread.isDone();
    }

    @Override
    public void cancelWork() {

        if (isWorking())
            workThread.cancel();
    }

    /**
     * Used for usage statistics logging.
     */
    public float getServerProcessingTime() {

        return serverProcessingTime;
    }

    /**
     * Used for usage statistics logging.
     */
    public void setServerProcessingTime(float serverProcessingTime) {

        this.serverProcessingTime = serverProcessingTime;
    }

    /**
     * Used for usage statistics logging.
     */
    public float getProcessingTime() {

        return (processingStopTime - processingStartTime) / 1000f;
    }

    /**
     * Used for usage statistics logging.
     */
    public void startProcessing() {

        this.processingStartTime = System.currentTimeMillis();
    }

    /**
     * Used for usage statistics logging.
     */
    public void stopProcessing() {

        this.processingStopTime = System.currentTimeMillis();
    }

    public void startLogin(String username, String password, ILoginListener listener) {

        cancelWork();

        workThread = new LoginThread(this, username, password);
        ((LoginThread) workThread).addListener(listener);
        ((LoginThread) workThread).addListener(new InternalLoginListener(username, password));
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }

    @Override
    public void startSearch(String query, MatchEnvironment env, ISearchListener listener) {

        cancelWork();

        long waitTime = TIMEOUT - (System.currentTimeMillis() - lastPostTime);

        workThread = new SearchThread(this, env, query, Math.max(0, waitTime), listener);
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }

    public void startSubmitEffect(Effect effect, boolean isPublic, ISubmitEffectListener listener) {

        cancelWork();

        long waitTime = TIMEOUT - (System.currentTimeMillis() - lastPostTime);

        workThread = new SubmitEffectThread(this, effect, isPublic, Math.max(0, waitTime), listener);
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }

    public void startSendFeedback(String query, MatchNode result, String comment, int rating, boolean flag,
            boolean isLog, boolean isStartup, long clientId, boolean used, ISendFeedbackListener listener) {

        cancelWork();

        long waitTime = TIMEOUT - (System.currentTimeMillis() - lastPostTime);

        workThread = new SendFeedbackThread(this, query, result, comment, rating, flag, isLog, isStartup, clientId,
                used, Math.max(0, waitTime), listener);
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }

    public void startLoadProfile(ILoadProfileListener listener) {

        cancelWork();

        long waitTime = TIMEOUT - (System.currentTimeMillis() - lastPostTime);

        workThread = new LoadProfileThread(this, Math.max(0, waitTime), listener);
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }

    public void startDeleteEffect(Effect effect, IDeleteEffectListener listener) {

        cancelWork();

        long waitTime = TIMEOUT - (System.currentTimeMillis() - lastPostTime);

        workThread = new DeleteEffectThread(this, effect, Math.max(0, waitTime), listener);
        workThread.start();
        lastPostTime = System.currentTimeMillis();
    }
}
