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
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.absent;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.recommenders.models.Coordinates.tryNewProjectCoordinate;
import static org.eclipse.recommenders.models.DependencyType.JAR;
import static org.eclipse.recommenders.utils.Versions.canonicalizeVersion;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Fingerprints;
import org.eclipse.recommenders.utils.Urls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class MavenCentralFingerprintSearchAdvisor extends AbstractProjectCoordinateAdvisor {

    public static final URL SEARCH_MAVEN_ORG = Urls.toUrl("http://search.maven.org/solrsearch");

    private static final Logger LOG = LoggerFactory.getLogger(MavenCentralFingerprintSearchAdvisor.class);

    private static final int NETWORK_TIMEOUT = (int) SECONDS.toMillis(3);

    private static final List<String> SUPPORTED_PACKAGINGS = Arrays.asList("jar", "war", "bundle");

    private static final String FIELD_GROUP_ID = "g";
    private static final String FIELD_ARTIFACT_ID = "a";
    private static final String FIELD_VERSION = "v";
    private static final String FIELD_PACKAGING = "p";

    private SolrServer server;

    public MavenCentralFingerprintSearchAdvisor() {
        this(null, -1);
    }

    public MavenCentralFingerprintSearchAdvisor(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null);
    }

    public MavenCentralFingerprintSearchAdvisor(String proxyHost, int proxyPort, String proxyUser) {
        this(proxyHost, proxyPort, proxyUser, null);
    }

    public MavenCentralFingerprintSearchAdvisor(String proxyHost, int proxyPort, String proxyUser, String proxyPassword) {
        server = createSolrServer(proxyHost, proxyPort, proxyUser, proxyPassword);
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

    public void setProxy(String host, int port, String user, String password) {
        server = createSolrServer(host, port, user, password);
    }

    private CommonsHttpSolrServer createSolrServer(String proxyHost, int proxyPort, String proxyUser,
            String proxyPassword) {
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        // Need to set these on the connection manager ourselves rather than using
        // CommonsHttpSolrServer.setConnectionTimeout and CommonsHttpSolrServer.setSoTimeout, as the bundle
        // org.apache.solr.client.solrj is missing a necessary import of package org.apache.commons.httpclient.params.
        // See <https://bugs.eclipse.org/bugs/show_bug.cgi?id=413496>.
        connectionManager.getParams().setConnectionTimeout(NETWORK_TIMEOUT);
        connectionManager.getParams().setSoTimeout(NETWORK_TIMEOUT);

        final HttpClient httpClient = new HttpClient(connectionManager);

        if (proxyHost != null) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }
        if (proxyUser != null) {
            Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            httpClient.getState().setProxyCredentials(AuthScope.ANY, credentials);
        }

        final CommonsHttpSolrServer server = new CommonsHttpSolrServer(SEARCH_MAVEN_ORG, httpClient);

        server.setAllowCompression(true);
        server.setParser(new XMLResponseParser());

        return server;
    }
}
