/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.server.codesearch.couchdb;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.commons.codesearch.SnippetSummary;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

public class CouchDbDataAccessService implements IDataAccessService {

    private static String escapedQuotation;
    private static String emptyObject;

    static {
        try {
            escapedQuotation = URLEncoder.encode("\"", "UTF-8");
            emptyObject = URLEncoder.encode("{}", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throwUnhandledException(e);
        }
    }

    private final String baseUrl = "http://localhost:5984/codesearch/";
    private final Client client = new Client();

    @Override
    public void saveSnippet(final SnippetSummary snippet) {

        final Builder builder = createRequestBuilder(encode(snippet.id));
        builder.put(SnippetSummary.class, snippet);
    }

    private Builder createRequestBuilder(final String path) {
        return client.resource(baseUrl + path).accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON);
    }

    @Override
    public SnippetSummary getSnippet(final String codeSnippetId) {

        return null;
    }

    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    // @Override
    // public TopicTeaserResult getTopicTeaserResult(final int startIndex, final
    // int limit) {
    // final String path = "_design/topic-teaser/_view/all?skip=" + startIndex +
    // "&limit=" + limit;
    // return getTopicTeaserResult(path);
    // }
    //
    // @Override
    // public TopicTeaserResult getLatestTopicTeaserResult(final int startIndex,
    // final int limit) {
    // final String path = "_design/topic-teaser/_view/by_creationDate?skip=" +
    // startIndex + "&limit=" + limit
    // + "&descending=true";
    // return getTopicTeaserResult(path);
    // }
    //
    // @Override
    // public TopicTeaser getTopicTeaser(final String topicId) {
    // final String path = "_design/topic-teaser/_view/all?key=" +
    // escapedQuotation + encode(escapeQuotes(topicId))
    // + escapedQuotation;
    // final List<TopicTeaser> result = query(path, new
    // GenericType<GenericResultObjectView<TopicTeaser>>() {
    // });
    // ensureIsTrue(result.size() <= 1,
    // "Unexpected number of rows returned by the database.");
    // if (result.size() == 1) {
    // return result.iterator().next();
    // } else {
    // return null;
    // }
    // }
    //
    // private TopicTeaserResult getTopicTeaserResult(final String path) {
    // final Builder builder = createRequestBuilder(path);
    // final GenericResultObjectView<TopicTeaser> view = builder
    // .get(new GenericType<GenericResultObjectView<TopicTeaser>>() {
    // });
    //
    // final TopicTeaserResult result = new TopicTeaserResult();
    // result.totalTopics = view.total_rows;
    // result.topics = transform(view.rows);
    // return result;
    // }
    //
    // @Override
    // public Topic getTopic(final String topicId) {
    // final String path = "_design/topics/_view/all?key=" + escapedQuotation +
    // encode(escapeQuotes(topicId))
    // + escapedQuotation;
    //
    // final Collection<Topic> result = query(path, new
    // GenericType<GenericResultObjectView<Topic>>() {
    // });
    // ensureIsTrue(result.size() <= 1,
    // "Unexpected number of rows returned by the database.");
    // if (result.size() == 1) {
    // return result.iterator().next();
    // } else {
    // return null;
    // }
    // }
    //
    // @Override
    // public List<Topic> getTopics() {
    // final String path = "_design/topics/_view/all";
    // return query(path, new GenericType<GenericResultObjectView<Topic>>() {
    // });
    // }
    //
    // @Override
    // public TransactionResult save(final Topic topic) {
    // final Builder builder = createRequestBuilder(encode(topic.id));
    // return builder.put(TransactionResult.class, topic);
    // }
    //
    // @Override
    // public TransactionResult deleteTopic(final String topicId, final String
    // rev) {
    // final String path = encode(topicId) + "?rev=" + rev;
    // return createRequestBuilder(path).delete(TransactionResult.class);
    // }
    //
    // @Override
    // public List<Post> getPosts(final String topicId) {
    // final String path = "_design/posts/_view/all?startkey=[" +
    // escapedQuotation + encode(escapeQuotes(topicId))
    // + escapedQuotation + "]&endkey=[" + escapedQuotation +
    // encode(escapeQuotes(topicId)) + escapedQuotation
    // + "," + emptyObject + "]";
    // return query(path, new GenericType<GenericResultObjectView<Post>>() {
    // });
    // }
    //
    // @Override
    // public List<Stacktrace> getStacktraces(final String topicId) {
    // final String path = "_design/stacktraces/_view/all?startkey=[" +
    // escapedQuotation
    // + encode(escapeQuotes(topicId)) + escapedQuotation + "]&endkey=[" +
    // escapedQuotation
    // + encode(escapeQuotes(topicId)) + escapedQuotation + "," + emptyObject +
    // "]";
    // return query(path, new GenericType<GenericResultObjectView<Stacktrace>>()
    // {
    // });
    // }
    //
    // @Override
    // public List<Comment> getComments(final String topicId, final int
    // postIndex) {
    // final String path = "_design/comments/_view/all?startkey=[" +
    // escapedQuotation + encode(escapeQuotes(topicId))
    // + escapedQuotation + "," + postIndex + "]&endkey=[" + escapedQuotation +
    // encode(escapeQuotes(topicId))
    // + escapedQuotation + "," + postIndex + "," + emptyObject + "]";
    // return query(path, new GenericType<GenericResultObjectView<Comment>>() {
    // });
    // }
    //
    // @Override
    // public User getUser(final String userId) {
    // try {
    // final String path = "_design/users/_view/all?key=" + escapedQuotation
    // + URLEncoder.encode(escapeQuotes(userId), "UTF-8") + escapedQuotation;
    // final List<User> result = query(path, new
    // GenericType<GenericResultObjectView<User>>() {
    // });
    // ensureIsTrue(result.size() <= 1,
    // "Unexpected number of rows returned by the database.");
    // if (result.size() == 1) {
    // return result.iterator().next();
    // } else {
    // return null;
    // }
    // } catch (final UnsupportedEncodingException e) {
    // throw new RuntimeException(e);
    // }
    // }
    //
    // @Override
    // public TransactionResult save(final User user) {
    // try {
    // final String path = URLEncoder.encode(user.id, "UTF-8");
    // final Builder builder = createRequestBuilder(path);
    // return builder.put(TransactionResult.class, user);
    // } catch (final UnsupportedEncodingException e) {
    // throw new RuntimeException(e);
    // }
    // }

}
