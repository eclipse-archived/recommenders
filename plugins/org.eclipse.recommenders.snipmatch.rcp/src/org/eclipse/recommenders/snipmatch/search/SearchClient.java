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

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.web.IDeleteEffectListener;
import org.eclipse.recommenders.snipmatch.web.ILoadProfileListener;
import org.eclipse.recommenders.snipmatch.web.ILoginListener;
import org.eclipse.recommenders.snipmatch.web.ISearchListener;
import org.eclipse.recommenders.snipmatch.web.ISendFeedbackListener;
import org.eclipse.recommenders.snipmatch.web.ISubmitEffectListener;

public interface SearchClient {

    void startSearch(String query, MatchEnvironment env, ISearchListener listener);

    void cancelWork();

    boolean isWorking();

    boolean isLoggedIn();

    void logout();

    String getUsername();

    String getPassword();

    String getName();

    String getVersion();

    void startProcessing();

    void stopProcessing();

    void setServerProcessingTime(float serverProcessingTime);

    float getServerProcessingTime();

    float getProcessingTime();

    void startSendFeedback(String query, MatchNode result, String comment, int rating, boolean flag, boolean isLog,
            boolean isStartup, long clientId, boolean used, ISendFeedbackListener listener);

    void startLogin(String username, String password, ILoginListener listener);

    void startDeleteEffect(Effect effect, IDeleteEffectListener listener);

    void startSubmitEffect(Effect effect, boolean isPublic, ISubmitEffectListener listener);

    void startLoadProfile(ILoadProfileListener listener);
}
