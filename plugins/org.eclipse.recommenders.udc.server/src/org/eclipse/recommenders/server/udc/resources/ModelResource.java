/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.server.udc.resources;

import static org.eclipse.recommenders.utils.Checks.ensureIsFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommenders.internal.server.udc.wiring.GuiceModule.ModelLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path("/model")
public class ModelResource {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@ModelLocation
	@Inject
	private File modelDirectory;

	@Inject
	private CouchDBAccessService couchdb;

	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Path("/{manifestIdentifier}")
	@GET
	public InputStream getModel(@PathParam("manifestIdentifier") final String manifestIdentifier) {
		log.info("Streaming model for {}.", manifestIdentifier);
		try {
			final File zipFile = getExistingZipFile(manifestIdentifier);
			final InputStream s = new FileInputStream(zipFile);
			return s;
		} catch (final FileNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/size/{manifestIdentifier}")
	@GET
	public Long getFileSize(@PathParam("manifestIdentifier") final String manifestIdentifier) {
		final File zipFile = getExistingZipFile(manifestIdentifier);
		return zipFile.length();
	}

	private File getExistingZipFile(final String manifestIdentifier) {
		ensureValidManifestIdentifier(manifestIdentifier);
		final File file = new File(modelDirectory, manifestIdentifier + ".zip").getAbsoluteFile();
		if (!file.exists()) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return file;
	}

	private void ensureValidManifestIdentifier(final String manifestIdentifier) {
		ensureIsFalse(manifestIdentifier.contains("/"), "Invalid Manifest Identifier. '/' is invalid character.");
	}

	@GET
	public String getObjectUsagesPerModel() {
		final Map<ModelSpecification, Integer> usagesPerModel = Maps.newHashMap();
		final Collection<ModelSpecification> modelSpecs = couchdb.getModelSpecifications();
		for (final ModelSpecification modelSpec : modelSpecs) {
			// TODO wieder reinnehmen...
			// if (modelSpec.getLastBuilt().hasValue()) {
			final Set<String> fingerprints = modelSpec.getFingerprints();
			final int numberOfObjectUsages = couchdb.getNumberOfObjectUsages(fingerprints);
			usagesPerModel.put(modelSpec, numberOfObjectUsages);
			// }
		}
		return renderHtml(usagesPerModel);
	}

	private String renderHtml(final Map<ModelSpecification, Integer> usagesPerModel) {
		final List<ModelSpecification> modelSpecs = Lists.newLinkedList(usagesPerModel.keySet());
		Collections.sort(modelSpecs, new Comparator<ModelSpecification>() {
			@Override
			public int compare(final ModelSpecification arg0, final ModelSpecification arg1) {
				final int compare = arg0.getSymbolicName().compareTo(arg1.getSymbolicName());
				if (compare == 0) {
					return arg0.getVersionRange().getMinVersion().compareTo(arg1.getVersionRange().getMinVersion());
				} else {
					return compare;
				}
			}
		});

		final StringBuilder result = new StringBuilder();
		renderHeader(result);
		for (final ModelSpecification modelSpec : modelSpecs) {
			renderModelSpec(result, modelSpec, usagesPerModel.get(modelSpec));
		}
		renderFooter(result);
		return result.toString();
	}

	private void renderHeader(final StringBuilder result) {
		result.append("<html><body><table><tr><th>Symbolic Name</th><th>Versions</th><th>ObjectUsages</th></tr>");
	}

	private void renderModelSpec(final StringBuilder result, final ModelSpecification modelSpec, final int usages) {
		result.append("<tr><td>");
		result.append(modelSpec.getSymbolicName());
		result.append("</td><td>");
		result.append(modelSpec.getVersionRange());
		result.append("</td><td align=\"right\">");
		result.append(usages);
		result.append("</td></tr>");
	}

	private void renderFooter(final StringBuilder result) {
		result.append("</table></body></html>");
	}

}
