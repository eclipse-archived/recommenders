/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.web;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.recommenders.snipmatch.core.Effect;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Sends a request asynchronously to submit a new effect to the SnipMatch repository.
 */
class SubmitEffectThread extends PostThread {

    private Effect effect;
    private boolean isPublic;
    private long waitTime;
    private ISubmitEffectListener listener;

    public SubmitEffectThread(RemoteMatchClient client, Effect effect, boolean isPublic, long waitTime,
            ISubmitEffectListener listener) {

        super(client, RemoteMatchClient.SUBMIT_URL);
        this.effect = effect;
        this.isPublic = isPublic;
        this.waitTime = waitTime;
        this.listener = listener;
    }

    @Override
    public void run() {

        if (!client.isLoggedIn()) {
            listener.submitEffectFailed("User not authenticated.");
            done = true;
            return;
        }

        try {
            sleep(waitTime);
        } catch (Exception e) {
            e.printStackTrace();
            listener.submitEffectFailed("Client thread error.");
            done = true;
            return;
        }

        String effectString = buildEffectString();
        // System.out.println(effectString);

        if (effectString == null) {
            listener.submitEffectFailed("Client XML error.");
            done = true;
            return;
        }

        addParameter("username", client.getUsername());
        addParameter("password", client.getPassword());
        addParameter("clientName", client.getName());
        addParameter("clientVersion", client.getVersion());
        addParameter("effectXML", effectString);
        addParameter("public", isPublic ? "true" : "false");

        if (done)
            return;

        InputStream response = post();

        if (response == null) {
            listener.submitEffectFailed("Connection error.");
            done = true;
            return;
        }

        if (done)
            return;

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document responseXml;

        try {

            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            responseXml = db.parse(response);
        } catch (Exception e) {

            e.printStackTrace();
            listener.submitEffectFailed("Bad response format.");
            done = true;
            return;
        }

        String msg = responseXml.getDocumentElement().getTextContent();

        if (msg.equals("Effect inserted.")) {
            listener.submitEffectSucceeded();
        } else {
            listener.submitEffectFailed(msg);
        }

        done = true;
    }

    private String buildEffectString() {

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Element effectXml = MatchConverter.writeEffect(doc, effect);
        doc.appendChild(effectXml);

        StringWriter sw = new StringWriter();

        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.transform(new DOMSource(doc), new StreamResult(sw));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sw.toString();
    }
}
