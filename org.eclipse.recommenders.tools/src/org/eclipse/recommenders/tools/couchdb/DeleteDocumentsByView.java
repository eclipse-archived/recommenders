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
package org.eclipse.recommenders.tools.couchdb;

import java.util.List;

import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ResultObject;
import org.eclipse.recommenders.commons.client.TransactionResult;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Throws;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class DeleteDocumentsByView {

	private static String couchdb = "http://localhost:5984/udc/";
//	private static String couchdb = "http://recommenders1.st.informatik.tu-darmstadt.de:5984/udc/";
	private static String view = "_design/metaData/_view/modelSpecifications";

	public static void main(final String[] args) {
		final ClientConfiguration config = ClientConfiguration.create(couchdb);
		final WebServiceClient client = new WebServiceClient(config);

		final GenericResultObjectView<?> result = client.doGetRequest(view,
				new GenericType<GenericResultObjectView<?>>() {
				});
		for (final ResultObject<?> document : result.rows) {
			final String revision = getRevision(client, document.id);
			// Remove comment to really delete documents. As this is a evil
			// function it should not be committed without comment

			// client.doDeleteRequest(document.id + "?rev=" + revision, TransactionResult.class);
			// System.out.println("Deleted " + document.id);
		}
	}

	private static String getRevision(final WebServiceClient client, final String id) {
		final ClientResponse response = client.createRequestBuilder(id).head();
		if (response.getHeaders().containsKey("Etag")) {
			final List<String> etags = response.getHeaders().get("Etag");
			if (etags.size() > 0) {
				return etags.get(0).replace("\"", "");
			}
		}
		throw Throws.throwIllegalStateException("Can't retrieve revision of document: " + id);
	}
}
