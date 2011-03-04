/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.codesearch;

import java.util.Date;
import java.util.List;

import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;

import com.google.gson.annotations.SerializedName;

public class RequestLogEntry extends Request {

    /**
     * Request id. Created by the server.
     */
    @SerializedName("_id")
    public String id;

    @SerializedName("_rev")
    public String rev;

    /**
     * The date the query was issued on. This field is set by the server and
     * used for logging purpose only.
     */
    public Date issuedOn;

    /**
     * The proposals made by the server. This field used for logs only and thus
     * is typically <code>null</code> on client side.
     */
    public List<Proposal> proposals;

    /**
     * The feedbacks collected on server side. This field used for logs only and
     * thus is typically <code>null</code> on client side.
     */
    public List<Feedback> feedbacks;

    public static RequestLogEntry newEntryFromRequest(final Request request) {
        final RequestLogEntry logEntry = new RequestLogEntry();
        logEntry.issuedBy = request.issuedBy;
        logEntry.query = request.query;
        logEntry.type = request.type;
        logEntry.featureWeights = request.featureWeights;
        return logEntry;
    }
}
