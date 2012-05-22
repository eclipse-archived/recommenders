/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.web;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.recommenders.snipmatch.search.SearchClient;

/**
 * A class for sending post messages asynchronously.
 */
public abstract class PostThread extends Thread {

    private PostMethod post;

    protected SearchClient client;
    protected boolean done;

    public PostThread(SearchClient client, String url) {

        this.client = client;
        post = new PostMethod(url);
        done = false;
    }

    public boolean isDone() {

        return done;
    }

    public void addParameter(String name, String value) {

        post.addParameter(name, value);
    }

    protected InputStream post() {

        HttpClient http = new HttpClient();
        http.getParams().setConnectionManagerTimeout(10000);

        if (done)
            return null;

        try {
            if (http.executeMethod(post) == -1)
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (done)
            return null;

        try {
            // System.out.println(post.getResponseBodyAsString());
            return post.getResponseBodyAsStream();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cancel() {

        done = true;
    }
}