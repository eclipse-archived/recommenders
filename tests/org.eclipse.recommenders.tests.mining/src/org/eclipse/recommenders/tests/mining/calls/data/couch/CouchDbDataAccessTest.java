/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.mining.calls.data.couch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.mining.calls.data.couch.CouchDbDataAccess;
import org.eclipse.recommenders.utils.VersionRange;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.results.TransactionResult;
import org.junit.Before;
import org.junit.Test;

public class CouchDbDataAccessTest {
	public CouchDbDataAccess uut;
	private WebServiceClient client;
	private TransactionResult result;

	@Before
	public void setup() {
		client = mock(WebServiceClient.class);
		uut = new CouchDbDataAccess(client);
	}

	@Test
	public void idAndRevisionAreSetIfModelIsSaved() {
		result = mockResult();

		ModelSpecification spec = new ModelSpecification("abc", new String[0], VersionRange.EMPTY, new Date(),
				new HashSet<String>());

		uut.save(spec);

		assertNotNull(spec._id);
		assertTrue(spec._id.equals(result.id));
		assertNotNull(spec._rev);
		assertTrue(spec._rev.equals(result.rev));
	}

	private TransactionResult mockResult() {
		TransactionResult result = new TransactionResult();
		result.id = "someId";
		result.rev = "someRev";
		when(client.doPostRequest(any(String.class), any(ModelSpecification.class), eq(TransactionResult.class)))
				.thenReturn(result);
		return result;
	}
}
