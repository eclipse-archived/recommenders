/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.server.codesearch;

import java.util.List;

import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.internal.server.codesearch.couchdb.TransactionResult;

public interface IDataAccessService {

    public TransactionResult save(RequestLogEntry request);

    public SnippetSummary getCodeSnippet(String snippetId);

    public List<RequestLogEntry> getLogEntries();

    public RequestLogEntry getLogEntry(String requestId);

}
