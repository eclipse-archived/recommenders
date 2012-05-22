/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.web;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Sends a request asynchronously to retrieve a user's own list of effects (snippets).
 */
class LoadProfileThread extends PostThread {

    private long waitTime;
    private ILoadProfileListener listener;

    public LoadProfileThread(RemoteMatchClient client, long waitTime, ILoadProfileListener listener) {

        super(client, RemoteMatchClient.PROFILE_URL);
        this.waitTime = waitTime;
        this.listener = listener;
    }

    @Override
    public void run() {

        if (!client.isLoggedIn()) {
            listener.loadProfileFailed("User not authenticated.");
            done = true;
            return;
        }

        try {
            sleep(waitTime);
        } catch (Exception e) {
            e.printStackTrace();
            listener.loadProfileFailed("Client thread error.");
            done = true;
            return;
        }

        if (done)
            return;

        addParameter("username", client.getUsername());
        addParameter("password", client.getPassword());
        addParameter("clientName", client.getName());
        addParameter("clientVersion", client.getVersion());

        InputStream response = post();

        if (done)
            return;

        if (response == null) {
            listener.loadProfileFailed("Connection error.");
            done = true;
            return;
        }

        if (done)
            return;

        boolean success = parseResults(response);

        if (done)
            return;

        if (!success)
            listener.loadProfileFailed("Bad response format.");
        else
            listener.loadProfileSucceeded();

        done = true;
    }

    private boolean parseResults(InputStream input) {

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document resultsXml;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            resultsXml = db.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        NodeList effectsNodes = resultsXml.getElementsByTagName("effectList");
        Element effectsNode = (Element) effectsNodes.item(0);
        NodeList effectNodes = effectsNode.getElementsByTagName("effect");

        Effect[] effects = new Effect[effectNodes.getLength()];

        for (int i = 0; i < effectNodes.getLength(); i++) {

            if (done)
                return true;

            try {
                effects[i] = MatchConverter.parseEffect((Element) effectNodes.item(i));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            if (done)
                return true;

            listener.effectLoaded(effects[i]);
        }

        return true;
    }
}