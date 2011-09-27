/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.rcp.selfhosting;

import static com.google.common.base.Charsets.UTF_8;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.filefilter.TrueFileFilter.INSTANCE;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.eclipse.recommenders.rcp.selfhosting.Activator.BUNDLE_ID;
import static org.eclipse.recommenders.rcp.utils.LoggingUtils.newInfo;
import static org.eclipse.recommenders.rcp.utils.LoggingUtils.newWarning;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.recommenders.commons.client.NotFoundException;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.rcp.selfhosting.CommandsGuiceModule.LocalCouchdb;

import com.google.common.io.Files;
import com.google.inject.Inject;

public class InitalizeCouchdb implements Callable<IStatus> {

    private final WebServiceClient client;
    private final File configArea;

    private final MultiStatus result = new MultiStatus(BUNDLE_ID, 0, "Operation Report", null);

    @Inject
    public InitalizeCouchdb(@LocalCouchdb final WebServiceClient client, @LocalCouchdb final File couchConfigurationArea) {
        this.client = client;
        this.configArea = couchConfigurationArea;
    }

    @Override
    public IStatus call() throws Exception {
        for (final File db : configArea.listFiles()) {
            findOrCreateDatabase(db.getName());
            final Iterator<File> it = iterateFiles(db, INSTANCE, INSTANCE);
            while (it.hasNext()) {
                final File next = it.next();
                if (shouldIgnore(next)) {
                    continue;
                }
                putDocument(next);
            }
        }
        return result;
    }

    private boolean shouldIgnore(final File next) {
        final String name = next.getName();
        return name.startsWith(".") || !name.endsWith(".json");
    }

    private void findOrCreateDatabase(final String databaseName) {
        try {
            client.doGetRequest(databaseName, TransactionResult.class);
        } catch (final NotFoundException nfe) {
            client.doPutRequest(databaseName, "", TransactionResult.class);
        }
    }

    private void putDocument(final File contentFile) throws IOException {

        String path = removeStart(contentFile.getAbsolutePath(), configArea.getAbsolutePath() + "/");
        path = StringUtils.removeEnd(path, ".json");
        try {
            final String content = Files.toString(contentFile, UTF_8);
            client.createRequestBuilder(path).type(TEXT_PLAIN_TYPE).put(content);
            result.add(newInfo(BUNDLE_ID, "Put %s to %s", path, client.getBaseUrl()));
        } catch (final Exception e) {
            final IStatus warn = newWarning(e, BUNDLE_ID, "Didn't put contents of %s. May already exist?: %s",
                    contentFile, e.getMessage());
            result.add(warn);
        }
    }
}
