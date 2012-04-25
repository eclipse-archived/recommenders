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

import org.eclipse.recommenders.snipmatch.core.EffectMatchNode;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * A misnomer. This class is used to send feedback as well as to log usage.
 */
public class SendFeedbackThread extends PostThread {
	
	String query;
	MatchNode match;
	String comment;
	int rating;
	boolean flag;
	boolean isLog;
	boolean isStartup;
	boolean used;
	long clientId;
	long waitTime;
	ISendFeedbackListener listener;

	public SendFeedbackThread(MatchClient client, String query, MatchNode match,
			String comment, int rating, boolean flag, boolean isLog, boolean isStartup, long clientId,
			boolean used, long waitTime, ISendFeedbackListener listener) {
		
		super(client, MatchClient.FEEDBACK_URL);
		this.query = query;
		this.match = match;
		this.comment = comment;
		this.rating = rating;
		this.flag = flag;
		this.isLog = isLog;
		this.isStartup = isStartup;
		this.clientId = clientId;
		this.used = used;
		this.waitTime = waitTime;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		
		if (!client.isLoggedIn() && !isStartup) {
			listener.sendFeedbackFailed("User not authenticated.");
			done = true;
			return;
		}

		try {
			sleep(waitTime);
		}
		catch (Exception e) {
			e.printStackTrace();
			listener.sendFeedbackFailed("Client thread error.");
			done = true;
			return;
		}

		String feedbackString = buildFeedbackString();
		
		if (feedbackString == null) {
			listener.sendFeedbackFailed("Client XML error.");
			done = true;
			return;
		}

		if (client.isLoggedIn()) {

			addParameter("username", client.getUsername());
			addParameter("password", client.getPassword());
		}

		addParameter("clientName", client.getName());
		addParameter("clientVersion", client.getVersion());
		addParameter("feedbackXML", feedbackString);
		addParameter("clientId", "" + clientId);

		if (isLog) addParameter("comment", "log");
		else if (isStartup) addParameter("comment", "startup");
		
		if (done) return;

		InputStream response = post();
		
		if (response == null) {
			listener.sendFeedbackFailed("Connection error.");
			done = true;
			return;
		}
		
		if (done) return;

		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document responseXml;

		try {

			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			responseXml = db.parse(response);
		}
		catch (Exception e) {
	
			e.printStackTrace();
			listener.sendFeedbackFailed("Bad response format.");
			done = true;
			return;
		}

		String msg = responseXml.getDocumentElement().getTextContent();
		
		if (msg.equals("Flagged.")) {
			listener.sendFeedbackSucceeded();
		}
		else {
			listener.sendFeedbackFailed(msg);
		}
		
		done = true;
	}
	
	private String buildFeedbackString() {
		
		if (isStartup) return "";
		
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document doc;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Element rootNode = doc.createElement("feedback");
		doc.appendChild(rootNode);
		
		rootNode.setAttribute("effect", "" + ((EffectMatchNode) match).getEffect().getId());

		Element queryNode = doc.createElement("query");
		rootNode.appendChild(queryNode);
		queryNode.setTextContent(query);
		
		if (isLog) {

			rootNode.setAttribute("serverTime", "" + client.getServerProcessingTime());
			rootNode.setAttribute("clientTime", "" + client.getProcessingTime());
			rootNode.setAttribute("used", "" + used);
		}
		else {
		
			rootNode.setAttribute("rating", "" + rating);
			rootNode.setAttribute("flag", "" + flag);
			
			Element commentNode = doc.createElement("comment");
			rootNode.appendChild(commentNode);
			if (comment != null) commentNode.setTextContent(comment);
		}
		
		StringWriter sw = new StringWriter();

		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.transform(new DOMSource(doc), new StreamResult(sw));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return sw.toString();
	}
}
