/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Johannes Dorn - Initial API and implementation
 */
package org.eclipse.recommenders.internal.news.rcp;

import static java.text.MessageFormat.format;

import java.util.Date;

class XmlBuilder {

    private final StringBuilder sb;

    public XmlBuilder() {
        sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><rss version=\"2.0\" xml:base=\"http://www.example.org\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:fb=\"http://www.facebook.com/2008/fbml\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:og=\"http://ogp.me/ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:sioc=\"http://rdfs.org/sioc/ns#\" xmlns:sioct=\"http://rdfs.org/sioc/types#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:schema=\"http://schema.org/\">");
        sb.append("<channel>");
        sb.append("<title>Blog</title>");
        sb.append("<link>http://www.example.org</link>");
        sb.append("<description></description>");
        sb.append("<language>en</language>");
    }

    public XmlBuilder addEntry(Date date, String title, String url, String... tags) {
        sb.append("<item>");
        sb.append(format("<title>{0}</title>", title));
        sb.append(format("<link>{0}</link>", url.toString()));
        sb.append(format("<description>{0}</description>", "Description"));
        sb.append(format("<pubDate>{0}</pubDate>", RssParser.DATE_FORMAT.format(date)));
        for (String tag : tags) {
            sb.append(format("<category domain=\"http://www.example.org\">{0}</category>", tag));
        }
        sb.append("</item>");
        return this;
    }

    public String build() {
        sb.append("</channel>");
        sb.append("</rss>");
        return sb.toString();
    }
}
