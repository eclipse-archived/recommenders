package org.eclipse.recommenders.tests.completion.rcp.calls.preferences;

import static org.eclipse.recommenders.utils.Version.UNKNOWN;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.eclipse.recommenders.internal.completion.rcp.calls.preferences.VersionLabelProvider;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VersionLabelProviderTest {

    Image IMG_FOUND = new Image(Display.getDefault(), 1, 1);
    Image IMG_NOT_FOUND = new Image(Display.getDefault(), 2, 2);
    VersionLabelProvider sut = new VersionLabelProvider(IMG_NOT_FOUND, IMG_FOUND);
    @Mock
    ClasspathEntryInfo info;
    private Tuple<ClasspathEntryInfo, Object> data;

    @Before
    public void before() {
        data = Tuple.newTuple(info, null);
    }

    @Test
    public void test01() {
        assertSame(IMG_NOT_FOUND, sut.getImage(data));
        assertSame(sut.NLS_UNKNOWN, sut.getToolTipText(data));
    }

    @Test
    public void test02() {
        when(info.getSymbolicName()).thenReturn("some");
        when(info.getVersion()).thenReturn(UNKNOWN);
        assertSame(IMG_NOT_FOUND, sut.getImage(data));
        assertSame(sut.NLS_UNKNOWN, sut.getToolTipText(data));
    }

    @Test
    public void test03() {
        when(info.getSymbolicName()).thenReturn("some");
        when(info.getVersion()).thenReturn(Version.valueOf("1"));
        assertSame(IMG_FOUND, sut.getImage(data));
        assertSame(sut.NLS_KNOWN, sut.getToolTipText(data));
    }
}
