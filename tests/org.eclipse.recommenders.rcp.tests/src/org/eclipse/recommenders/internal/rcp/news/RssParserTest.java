package org.eclipse.recommenders.internal.rcp.news;

import static java.text.MessageFormat.format;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Test;

public class RssParserTest {

    private static final String TITLE_1 = "Title 1";
    private static final String URL_1 = "http://www.example.org";
    private static final String TITLE_2 = "Title 2";
    private static final String URL_2 = "http://www.example.com";
    private static final String TITLE_3 = "Title 3";
    private static final String URL_3 = "http://www.example.net";

    @Test
    public void testNullString() {
        List<Pair<String, URL>> entries = RssParser.getEntries(null, new Date());
        assertThat(entries.isEmpty(), is(true));
    }

    @Test
    public void testEmptyString() {
        List<Pair<String, URL>> entries = RssParser.getEntries("", new Date());
        assertThat(entries.isEmpty(), is(true));
    }

    @Test
    public void testNullDate() throws Exception {
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(new Date(), TITLE_1, URL_1);
        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), null);
        assertThat(entries.isEmpty(), is(true));
    }

    @Test
    public void testIllegalXml() {
        List<Pair<String, URL>> entries = RssParser.getEntries("<rss><item></rss>", new Date());
        assertThat(entries.isEmpty(), is(true));
    }

    @Test
    public void testSingleEntry() throws Exception {
        Date publishDate = DateUtils.addDays(new Date(), -1);
        Date lastCheckedDate = DateUtils.addDays(new Date(), -2);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(publishDate, TITLE_1, URL_1, RssParser.FILTER_TAG);

        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), lastCheckedDate);

        assertThat(entries.get(0).getFirst(), is(equalTo(TITLE_1)));
        assertThat(entries.get(0).getSecond(), is(equalTo(new URL(URL_1))));
        assertThat(entries.size(), is(1));
    }

    @Test
    public void testTwoEntries() throws Exception {
        Date publishDate1 = DateUtils.addDays(new Date(), -1);
        Date publishDate2 = DateUtils.addDays(new Date(), -2);
        Date lastCheckedDate = DateUtils.addDays(new Date(), -3);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(publishDate1, TITLE_1, URL_1, RssParser.FILTER_TAG);
        xml.addEntry(publishDate2, TITLE_2, URL_2, RssParser.FILTER_TAG);

        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), lastCheckedDate);

        assertThat(entries.get(0).getFirst(), is(equalTo(TITLE_1)));
        assertThat(entries.get(0).getSecond(), is(equalTo(new URL(URL_1))));
        assertThat(entries.get(1).getFirst(), is(equalTo(TITLE_2)));
        assertThat(entries.get(1).getSecond(), is(equalTo(new URL(URL_2))));
        assertThat(entries.size(), is(2));
    }

    @Test
    public void testTwoEntriesOneAlreadyChecked() throws Exception {
        Date publishDate1 = DateUtils.addDays(new Date(), -1);
        Date publishDate2 = DateUtils.addDays(new Date(), -3);
        Date lastCheckedDate = DateUtils.addDays(new Date(), -2);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(publishDate1, TITLE_1, URL_1, RssParser.FILTER_TAG);
        xml.addEntry(publishDate2, TITLE_2, URL_2, RssParser.FILTER_TAG);

        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), lastCheckedDate);

        assertThat(entries.get(0).getFirst(), is(equalTo(TITLE_1)));
        assertThat(entries.get(0).getSecond(), is(equalTo(new URL(URL_1))));
        assertThat(entries.size(), is(1));
    }

    @Test
    public void testTwoEntriesOneIllegal() throws Exception {
        Date publishDate1 = DateUtils.addDays(new Date(), -1);
        Date publishDate2 = DateUtils.addDays(new Date(), -2);
        Date lastCheckedDate = DateUtils.addDays(new Date(), -3);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(publishDate1, TITLE_1, URL_1, RssParser.FILTER_TAG);
        xml.addEntry(publishDate2, TITLE_2, "foo::bar", RssParser.FILTER_TAG);

        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), lastCheckedDate);

        assertThat(entries.get(0).getFirst(), is(equalTo(TITLE_1)));
        assertThat(entries.get(0).getSecond(), is(equalTo(new URL(URL_1))));
        assertThat(entries.size(), is(1));
    }

    @Test
    public void testFilterByTag() throws Exception {
        Date publishDate1 = DateUtils.addDays(new Date(), -1);
        Date publishDate2 = DateUtils.addDays(new Date(), -2);
        Date publishDate3 = DateUtils.addDays(new Date(), -3);
        Date lastCheckedDate = DateUtils.addDays(new Date(), -4);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(publishDate1, TITLE_1, URL_1);
        xml.addEntry(publishDate2, TITLE_2, URL_2, "some-tag");
        xml.addEntry(publishDate3, TITLE_3, URL_3, RssParser.FILTER_TAG);

        List<Pair<String, URL>> entries = RssParser.getEntries(xml.build(), lastCheckedDate);

        assertThat(entries.get(0).getFirst(), is(equalTo(TITLE_3)));
        assertThat(entries.get(0).getSecond(), is(equalTo(new URL(URL_3))));
        assertThat(entries.size(), is(1));
    }

    private class XmlBuilder {

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
}
