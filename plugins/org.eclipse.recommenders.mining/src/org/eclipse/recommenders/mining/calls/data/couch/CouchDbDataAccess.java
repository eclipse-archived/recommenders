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
package org.eclipse.recommenders.mining.calls.data.couch;

import static com.google.common.collect.Iterables.getFirst;
import static org.eclipse.recommenders.webclient.CouchUtils.createViewUrl;
import static org.eclipse.recommenders.webclient.CouchUtils.createViewUrlWithKey;
import static org.eclipse.recommenders.webclient.CouchUtils.transformValues;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.webclient.CouchUtils;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.results.GenericResultObjectView;
import org.eclipse.recommenders.webclient.results.TransactionResult;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public class CouchDbDataAccess {

    private final WebServiceClient client;

    @Inject
    public CouchDbDataAccess(final WebServiceClient client) {
        this.client = client;
    }

    public Date getLatestTimestampForFingerprint(final String fingerprint) {
        final String url = createViewUrlWithKey("objectUsages", "latestTimestampByFingerprint", fingerprint);
        final GenericResultObjectView<Date> queryResult = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<Date>>() {
                });
        final List<Date> res = CouchUtils.transformValues(queryResult);
        return getFirst(res, new Date(0));
    }

    public Collection<LibraryIdentifier> getLibraryIdentifiers() {
        final String url = createViewUrl("metaData", "libraryIdentifier");
        final GenericResultObjectView<LibraryIdentifier> result = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<LibraryIdentifier>>() {
                });
        return transformValues(result);
    }

    public Collection<LibraryIdentifier> getLibraryIdentifiersForSymbolicName(final String symbolicName) {
        final String url = createViewUrlWithKey("metaData", "libraryIdentifierByName", symbolicName);
        final GenericResultObjectView<LibraryIdentifier> result = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<LibraryIdentifier>>() {
                });
        return transformValues(result);
    }

    public Collection<ModelSpecification> getModelSpecifications() {
        final String url = createViewUrl("metaData", "modelSpecifications");
        final GenericResultObjectView<ModelSpecification> queryResult = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<ModelSpecification>>() {
                });
        return transformValues(queryResult);
    }

    public Collection<ModelSpecification> lookupAllModelSpecifications() {
        final String url = createViewUrl("metaData", "modelSpecifications");
        final GenericResultObjectView<ModelSpecification> queryResult = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<ModelSpecification>>() {
                });
        return transformValues(queryResult);
    }

    public List<ObjectUsage> getObjectUsages(final Set<String> fingerprints) {

        final List<ObjectUsage> result = Lists.newLinkedList();
        for (final String fingerprint : fingerprints) {
            final Collection<ObjectUsage> objectUsages = getObjectUsages(fingerprint);
            // System.out.printf("%s: %d\n", fingerprint, objectUsages.size());
            result.addAll(objectUsages);
        }
        return result;
    }

    private Collection<ObjectUsage> getObjectUsages(final String fingerprint) {
        // TODO remove stale
        final String url = CouchUtils.createViewUrlWithKey("objectUsages", "byFingerprint", fingerprint)
                + "&reduce=false";// &stale=ok";
        final GenericResultObjectView<ObjectUsage> queryResult = client.doGetRequest(url,
                new GenericType<GenericResultObjectView<ObjectUsage>>() {
                });
        return transformValues(queryResult);
    }

    public void save(final ModelSpecification modelSpec) {
        final TransactionResult result = client.doPostRequest("", modelSpec, TransactionResult.class);
        modelSpec._id = result.id;
        modelSpec._rev = result.rev;
        // System.out.println("");
    }
}
