package org.eclipse.recommenders.internal.news.rcp;

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
}
