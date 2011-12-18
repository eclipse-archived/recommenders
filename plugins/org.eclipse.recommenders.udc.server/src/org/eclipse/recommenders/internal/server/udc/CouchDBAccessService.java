/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.udc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.server.udc.wiring.GuiceModule.UdcScope;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.GenericResultObjectView;
import org.eclipse.recommenders.webclient.NotFoundException;
import org.eclipse.recommenders.webclient.ResultObject;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class CouchDBAccessService {

	private final WebServiceClient client;
	private final String escapedQuotation;

	@Inject
	public CouchDBAccessService(@UdcScope final ClientConfiguration config) {
		this.client = new WebServiceClient(config);
		// XXX: We don't support stale views ok yet:
		// if (staledViewsOk) {
		// client.addQueryParameter("stale=update_after");
		// }
		escapedQuotation = encode("\"");
	}

	protected String getRevision(final String id) {
		final ClientResponse response = client.createRequestBuilder(id).head();
		if (response.getHeaders().containsKey("Etag")) {
			final List<String> etags = response.getHeaders().get("Etag");
			if (etags.size() > 0) {
				return etags.get(0).replace("\"", "");
			}
		}
		return null;
	}

	protected String encode(final String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> List<T> transform(final GenericResultObjectView<T> queryResult) {
		final List<T> resultList = new ArrayList<T>(queryResult.rows.size());
		for (final ResultObject<T> row : queryResult.rows) {
			resultList.add(row.value);
		}
		return resultList;
	}

	public void save(final CompilationUnit[] newUnits) {
		final List<CompilationUnit> unitsToAddOrUpdate = Lists.newLinkedList();
		for (final CompilationUnit newUnit : newUnits) {
			final CompilationUnit oldUnit = getCompilationUnit(newUnit.id);
			if (oldUnit == null) {
				unitsToAddOrUpdate.add(newUnit);
			} else if (oldUnit.creationTimestamp.compareTo(newUnit.creationTimestamp) < 0) {
				newUnit.rev = oldUnit.rev;
				unitsToAddOrUpdate.add(newUnit);
			}
		}
		final UpdateQuery query = new UpdateQuery(unitsToAddOrUpdate);
		client.doPostRequest("_bulk_docs", query);
	}

	private CompilationUnit getCompilationUnit(final String id) {
		try {
			return client.doGetRequest(id, CompilationUnit.class);
		} catch (final NotFoundException e) {
			return null;
		}
	}

	public Collection<LibraryIdentifier> getLibraryIdentifiers() {
		final GenericResultObjectView<LibraryIdentifier> result = client.doGetRequest(
				"_design/metaData/_view/libraryIdentifier",
				new GenericType<GenericResultObjectView<LibraryIdentifier>>() {
				});
		return transform(result);
	}

	public LibraryIdentifier getLibraryIdentifierForFingerprint(final String fingerprint) {
		final GenericResultObjectView<LibraryIdentifier> result = client.doGetRequest(
				"_design/metaData/_view/libraryIdentifier?key=" + escapedQuotation + fingerprint + escapedQuotation,
				new GenericType<GenericResultObjectView<LibraryIdentifier>>() {
				});
		if (result.rows.size() > 0) {
			return result.rows.get(0).value;
		} else {
			return null;
		}
	}

	public Collection<LibraryIdentifier> getLibraryIdentifiersForSymbolicName(final String symbolicName) {
		final GenericResultObjectView<LibraryIdentifier> result = client.doGetRequest(
				"_design/metaData/_view/libraryIdentifierByName?key=" + escapedQuotation
						+ WebServiceClient.encode(symbolicName) + escapedQuotation,
				new GenericType<GenericResultObjectView<LibraryIdentifier>>() {
				});
		return transform(result);
	}

	public List<ObjectUsage> getObjectUsages(final Set<String> fingerprints) {
		final List<ObjectUsage> result = Lists.newLinkedList();
		for (final String fingerprint : fingerprints) {
			result.addAll(getObjectUsages(fingerprint));
		}
		return result;
	}

	private Collection<ObjectUsage> getObjectUsages(final String fingerprint) {
		final GenericResultObjectView<ObjectUsage> queryResult = client.doGetRequest(
				"_design/objectUsages/_view/byFingerprint?reduce=false&key=" + escapedQuotation + fingerprint
						+ escapedQuotation, new GenericType<GenericResultObjectView<ObjectUsage>>() {
				});
		return transform(queryResult);
	}

	public int getNumberOfObjectUsages(final Set<String> fingerprints) {
		int result = 0;
		for (final String fingerprint : fingerprints) {
			result += getNumberOfObjectUsages(fingerprint);
		}
		return result;
	}

	private int getNumberOfObjectUsages(final String fingerprint) {
		String path = "_design/objectUsages/_view/byFingerprint?reduce=true&key=" + escapedQuotation + fingerprint
				+ escapedQuotation;
		final GenericResultObjectView<Integer> queryResult = client.doGetRequest(path,
				new GenericType<GenericResultObjectView<Integer>>() {
				});
		if (queryResult.rows.size() == 1) {
			return queryResult.rows.get(0).value;
		} else {
			return 0;
		}
	}

	public Date getLatestTimestampForFingerprints(final Set<String> fingerprints) {
		Date latest = new Date(0);
		for (final String fingerprint : fingerprints) {
			final Date timestamp = getLatestTimestampForFingerprint(fingerprint);
			if (latest.compareTo(timestamp) <= 0) {
				latest = timestamp;
			}
		}
		return latest;
	}

	private Date getLatestTimestampForFingerprint(final String fingerprint) {
		final GenericResultObjectView<Date> queryResult = client.doGetRequest(
				"_design/objectUsages/_view/latestTimestampByFingerprint?key=" + escapedQuotation + fingerprint
						+ escapedQuotation, new GenericType<GenericResultObjectView<Date>>() {
				});
		if (queryResult.rows.size() > 0) {
			return queryResult.rows.get(0).value;
		} else {
			return new Date(0);
		}
	}

	public Collection<ModelSpecification> getModelSpecifications() {
		final GenericResultObjectView<ModelSpecification> queryResult = client.doGetRequest(
				"_design/metaData/_view/modelSpecifications",
				new GenericType<GenericResultObjectView<ModelSpecification>>() {
				});
		return transform(queryResult);
	}

	public Collection<ModelSpecification> getModelSpecificationsByNameOrAlias(final String symbolicName) {
		final GenericResultObjectView<ModelSpecification> queryResult = client.doGetRequest(
				"_design/metaData/_view/modelSpecificationsIncludeAlias?key=" + escapedQuotation
						+ WebServiceClient.encode(symbolicName) + escapedQuotation,
				new GenericType<GenericResultObjectView<ModelSpecification>>() {
				});
		return transform(queryResult);
	}

	public ModelSpecification getModelSpecificationByFingerprint(final String fingerprint) {
		final GenericResultObjectView<ModelSpecification> queryResult = client.doGetRequest(
				"_design/metaData/_view/modelSpecificationByFingerprint?key=" + escapedQuotation + fingerprint
						+ escapedQuotation, new GenericType<GenericResultObjectView<ModelSpecification>>() {
				});
		if (queryResult.rows.size() > 0) {
			return queryResult.rows.get(0).value;
		} else {
			return null;
		}
	}

	public void save(final LibraryIdentifier libraryIdentifier) {
		client.doPostRequest("", libraryIdentifier);
	}
}