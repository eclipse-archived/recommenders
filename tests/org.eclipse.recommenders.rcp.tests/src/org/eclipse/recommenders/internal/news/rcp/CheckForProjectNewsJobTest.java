package org.eclipse.recommenders.internal.news.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.rcp.Constants.*;
import static org.eclipse.recommenders.internal.rcp.Messages.NEWS_NOTIFY_MESSAGE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class CheckForProjectNewsJobTest {

    private static final String TITLE_1 = "Title 1";
    private static final String URL_1 = "http://www.example.org";
    private static final String TITLE_2 = "Title 2";
    private static final String URL_2 = "http://www.example2.org";

    private IEclipsePreferences prefs;
    private CheckForProjectNewsJob sut;

    @Before
    public void beforeTest() {
        prefs = mock(IEclipsePreferences.class);
        sut = new CheckForProjectNewsJob(prefs);
    }

    @Test
    public void testRunNotEnabled() {
        when(prefs.getBoolean(eq(NEWS_ENABLED), anyBoolean())).thenReturn(false);
        assertThat(sut.shouldRun(), is(false));
    }

    @Test
    public void testRecentlyRun() {
        Date lastShownDate = DateUtils.addDays(new Date(), -1);
        when(prefs.getBoolean(eq(NEWS_ENABLED), anyBoolean())).thenReturn(true);
        when(prefs.getLong(eq(NEWS_LAST_CHECK), anyLong())).thenReturn(lastShownDate.getTime());
        assertThat(sut.shouldRun(), is(false));
    }

    @Test
    public void testNotRecentlyRun() {
        Date lastShownDate = DateUtils.addDays(new Date(), -5);
        when(prefs.getBoolean(eq(NEWS_ENABLED), anyBoolean())).thenReturn(true);
        when(prefs.getLong(eq(NEWS_LAST_CHECK), anyLong())).thenReturn(lastShownDate.getTime());
        assertThat(sut.shouldRun(), is(true));
    }

    @Test
    public void testCreateNotificationLink() throws Exception {
        Pair<String, URL> stringURLpair = Pair.newPair(TITLE_1, new URL(URL_1));
        Pair<String, URL> stringURLpair2 = Pair.newPair(TITLE_2, new URL(URL_2));
        List<Pair<String, URL>> entries = Lists.newArrayList();
        entries.add(stringURLpair);
        entries.add(stringURLpair2);

        String link = format(NEWS_NOTIFY_MESSAGE, TITLE_1, new URL(URL_1));
        assertThat(sut.createNotificationLink(entries).equals(link), is(true));
    }

    @Test
    public void testCreateNotificationLinkNoEntries() throws Exception {
        List<Pair<String, URL>> entries = Collections.emptyList();

        assertThat(sut.createNotificationLink(entries).equals(""), is(true));
    }

    @Test
    public void testTimeStoredOnEmtpyResult() throws Exception {
        assertThat(sut.getNewsItems("").size() == 0, is(true));

        verify(prefs, times(1)).putLong(eq(NEWS_LAST_CHECK), anyLong());
    }

    @Test
    public void testEntryList() throws Exception {
        when(prefs.getLong(eq(NEWS_LAST_CHECK), anyLong())).thenReturn(0L);
        XmlBuilder xml = new XmlBuilder();
        xml.addEntry(new Date(), TITLE_1, URL_1, RssParser.FILTER_TAG);

        assertThat(sut.getNewsItems(xml.build()).size() == 1, is(true));
        verify(prefs, times(1)).putLong(eq(NEWS_LAST_CHECK), anyLong());
    }
}
