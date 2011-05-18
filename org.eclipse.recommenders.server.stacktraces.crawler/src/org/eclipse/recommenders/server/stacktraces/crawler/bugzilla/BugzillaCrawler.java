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
package org.eclipse.recommenders.server.stacktraces.crawler.bugzilla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.recommenders.server.stacktraces.crawler.Crawler;
import org.eclipse.recommenders.server.stacktraces.crawler.CrawlerConfiguration;
import org.eclipse.recommenders.server.stacktraces.crawler.Stacktrace;
import org.eclipse.recommenders.server.stacktraces.crawler.StacktraceOccurence;
import org.eclipse.recommenders.server.stacktraces.crawler.StacktraceParser;
import org.eclipse.recommenders.server.stacktraces.crawler.StorageService;
import org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.generated.Bug;
import org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.generated.Bugzilla;
import org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.generated.LongDesc;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.representation.Form;

public class BugzillaCrawler implements Crawler {

    private static final String CHARSET = "US-ASCII";
    private final Logger logger = Logger.getLogger(getClass());
    private final Unmarshaller unmarshaller;
    private final StorageService storage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private final SimpleDateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private CrawlerConfiguration configuration;

    @Inject
    public BugzillaCrawler(final StorageService storage) throws JAXBException {
        this.storage = storage;
        final JAXBContext jaxbContext = JAXBContext
                .newInstance("org.eclipse.recommenders.server.stacktraces.crawler.bugzilla.generated");
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    @Override
    public void configure(final CrawlerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void crawl(final Date start, final Date end) {
        loadBugs(getBugIdsBetween(start, end));
    }

    private List<String> getBugIdsBetween(final Date start, final Date end) {
        final LinkedList<String> bugIds = new LinkedList<String>();
        logger.info(String.format("Loading bugs between %s and %s", queryDateFormat.format(start),
                queryDateFormat.format(end)));
        try {
            final URL url = new URL(String.format(configuration.url
                    + "buglist.cgi?chfieldfrom=%s&chfieldto=%s&query_format=advanced&ctype=csv",
                    queryDateFormat.format(start), queryDateFormat.format(end)));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                final String bugId = line.substring(0, line.indexOf(","));
                bugIds.add(bugId);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return bugIds;
    }

    public void loadBugs(final Collection<String> bugIds) {
        try {
            final Form form = new Form();
            form.add("ctype", "xml");
            form.add("excludefield", "attachmentdata");
            form.add("id", StringUtils.join(bugIds, ","));

            final Client client = new Client();
            final String response = client.resource(configuration.url + "show_bug.cgi")
                    .type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form);
            final Bugzilla bugzilla = parse(response);
            if (bugzilla.getBug().size() == bugIds.size()) {
                for (final Bug bug : bugzilla.getBug()) {
                    parseBug(bug);
                }
            } else {
                throw new IllegalStateException("Xml result did not contain all bugs contained in the query.");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Bug loadBug(final int bugId) {
        try {
            final URL url = new URL(configuration.url + "show_bug.cgi?ctype=xml&id=" + bugId);
            final Bugzilla bugzilla = parse(url.openStream());
            final List<Bug> bugs = bugzilla.getBug();
            if (bugs.size() == 1) {
                logger.info("Loaded bug for bug id: " + bugId);
                return bugs.get(0);
            }
        } catch (final Exception e) {
            logger.warn("Could not load bug with bug id: " + bugId, e);
        }
        return null;
    }

    private Bugzilla parse(final InputStream inputStream) throws JAXBException, IOException {
        return parse(new InputStreamReader(inputStream, CHARSET));
    }

    private Bugzilla parse(final String content) throws IOException, JAXBException {
        return parse(new StringReader(content));
    }

    private Bugzilla parse(final Reader reader) throws IOException, JAXBException {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        final StringBuilder builder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            if (!line.startsWith("<!DOCTYPE")) {
                builder.append(line);
                builder.append("\n");
            }
        }
        return (Bugzilla) unmarshaller.unmarshal(new StringReader(builder.toString()));
    }

    private void parseBug(final Bug bug) {
        for (final LongDesc description : bug.getLongDesc()) {
            List<Stacktrace> stacktraces;
            try {
                stacktraces = StacktraceParser.parseAll(description.getThetext());
                int index = 0;
                for (final Stacktrace stacktrace : stacktraces) {
                    store(stacktrace, bug, index);
                    index++;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void store(final Stacktrace stacktrace, final Bug bug, final int index) throws java.text.ParseException {
        logger.info("Found stacktrace in bug with bug id: " + bug.getBugId());
        final StacktraceOccurence occurence = new StacktraceOccurence();
        occurence.source = configuration.nameOfSource;
        occurence.stacktrace = stacktrace;
        occurence.type = getClass().getSimpleName();
        occurence.url = configuration.url + "show_bug.cgi?id=" + bug.getBugId();
        occurence.lastModification = dateFormat.parse(bug.getDeltaTs());
        occurence.id = occurence.url + "-" + index;
        storage.store(occurence);
    }
}
