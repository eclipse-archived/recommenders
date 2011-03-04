/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.codesearch;


/**
 * The data structure containing all parameters required to issue a code search
 * query.
 */
public class Request {

    public static final Request INVALID = new Request();

    public static Request create() {

        return new Request();
    }

    /**
     * The unique user-id is generated from the user's mac address and hashed
     * using sha1. This is used internally for filtering operations - for
     * instance to determine new or frequent users of the system etc.)
     */
    public String issuedBy;

    /**
     * Different types of queries can be issued. Since each query type may use
     * different feature weights, this flags allows the server to quickly
     * identify the feature weight set to use.
     */
    public RequestType type;

    /**
     * This summary contains the information used to find relevant code
     * examples. It is typically created from a text selection of an Editor
     * inside Eclipse.
     */
    public SnippetSummary query;

    /**
     * A query may be customized using individual feature weights.
     */
    public FeatureWeights featureWeights;

}
