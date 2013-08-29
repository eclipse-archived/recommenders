/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.eclipse.recommenders.models.advisors.AbstractProjectCoordinateAdvisor;
import org.eclipse.recommenders.utils.Fingerprints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public class MavenCentralFingerprintSearchAdvisor extends AbstractProjectCoordinateAdvisor {

    private static final Logger LOG = LoggerFactory.getLogger(MavenCentralFingerprintSearchAdvisor.class);

    private static final URL SEARCH_MAVEN_ORG;

    private static final List<String> SUPPORTED_PACKAGINGS = Arrays.asList("jar", "war", "bundle");

    private static final String FIELD_GROUP_ID = "g";
    private static final String FIELD_ARTIFACT_ID = "a";
    private static final String FIELD_VERSION = "v";
    private static final String FIELD_PACKAGING = "p";

    static {
        try {
            SEARCH_MAVEN_ORG = new URL("http://search.maven.org/solrsearch");
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    private SolrServer server;

    public MavenCentralFingerprintSearchAdvisor() {
        server = createSolrServer();
    }

    @Override
    protected boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == JAR;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("1:\"" + Fingerprints.sha1(dependencyInfo.getFile()) + "\"");
            query.setRows(1);
            QueryResponse response = server.query(query);
            SolrDocumentList results = response.getResults();

            for (SolrDocument document : results) {
                if (!SUPPORTED_PACKAGINGS.contains(document.get(FIELD_PACKAGING))) {
                    continue;
                }

                String groupId = (String) document.get(FIELD_GROUP_ID);
                String artifactId = (String) document.get(FIELD_ARTIFACT_ID);
                String version = (String) document.get(FIELD_VERSION);

                return tryNewProjectCoordinate(groupId, artifactId, canonicalizeVersion(version));
            }

            return absent();
        } catch (SolrServerException e) {
            LOG.error("Exception when querying Solr Server <{}>", SEARCH_MAVEN_ORG, e);
            return absent();
        }
    }

    private SolrServer createSolrServer() {
        // Need to create our own HttpClient here rather than let CommonsHttpSolrServer do it, as that fails a with
        // ClassDefNotFoundError.
        // Apparently, there's an Import-Package missing for org.apache.commons.httpclient.params(.HttpMethodParams).
        final HttpClient httpClient = new HttpClient();
        final CommonsHttpSolrServer server = new CommonsHttpSolrServer(SEARCH_MAVEN_ORG, httpClient);

        server.setAllowCompression(true);
        server.setParser(new XMLResponseParser());

        return server;
    }
}
