/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.news;

import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.recommenders.internal.rcp.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.Pair;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class RssParser {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.UK); //$NON-NLS-1$
    protected static final String FILTER_TAG = "ide-news"; //$NON-NLS-1$

    public static List<Pair<String, URL>> getEntries(@Nullable String xml, @Nullable Date fromDate) {
        if (Strings.isNullOrEmpty(xml)) {
            return Collections.emptyList();
        }
        if (fromDate == null) {
            return Collections.emptyList();
        }
        List<Pair<String, URL>> entries = Lists.newArrayList();
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList items = (NodeList) xPath.compile("rss/channel/item").evaluate( //$NON-NLS-1$
                    new InputSource(new StringReader(xml)), XPathConstants.NODESET);
            for (int i = 0; i < items.getLength(); i++) {
                Pair<String, URL> item = getItem(xPath, items.item(i), fromDate).orNull();
                if (item != null) {
                    entries.add(item);
                }
            }
            return entries;
        } catch (Exception e) {
            Logs.log(LogMessages.WARNING_EXCEPTION_PARSING_NEWS_FEED, e);
            return Collections.emptyList();
        }
    }

    private static Optional<Pair<String, URL>> getItem(XPath xPath, Node item, Date fromDate) {
        try {
            Date date = DATE_FORMAT.parse(xPath.evaluate("pubDate", item)); //$NON-NLS-1$
            if (date.before(fromDate)) {
                return Optional.absent();
            }
            if (!hasTag(xPath, item, FILTER_TAG)) {
                return Optional.absent();
            }
            String title = xPath.evaluate("title", item); //$NON-NLS-1$
            URL url = new URL(xPath.evaluate("link", item)); //$NON-NLS-1$
            return Optional.of(Pair.newPair(title, url));
        } catch (Exception e) {
            Logs.log(LogMessages.WARNING_EXCEPTION_PARSING_NEWS_FEED_ITEM, e);
            return Optional.absent();
        }
    }

    private static boolean hasTag(XPath xPath, Node item, String tag) throws XPathExpressionException {
        NodeList tagList = (NodeList) xPath.evaluate("category", item, XPathConstants.NODESET); //$NON-NLS-1$
        boolean foundTag = false;
        for (int i = 0; i < tagList.getLength(); i++) {
            Node tagItem = tagList.item(i);
            if (tag.equals(tagItem.getFirstChild().getNodeValue())) {
                foundTag = true;
                break;
            }
        }
        return foundTag;
    }
}
