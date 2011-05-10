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
package org.eclipse.recommenders.tools;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;

public class StorageService {

    private final WebServiceClient dbClient;

    public StorageService(final ClientConfiguration configuration) {
        dbClient = new WebServiceClient(configuration);
    }

    public void store(final Archive archive) {
        archive.id = archive.fingerprint;
        final TransactionResult transactionResult = dbClient.doPutRequest(WebServiceClient.encode(archive.id), archive,
                TransactionResult.class);
    }
}
