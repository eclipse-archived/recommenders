package org.eclipse.recommenders.server.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public final class WikiServerTest {

    private static final String TESTINPUT = "This is a test using Button. I like using Button.";

    private final WikiServer server = new WikiServer();
    private final IJavaElement element;

    public WikiServerTest() {
        element = Mockito.mock(IJavaElement.class);
        Mockito.when(element.getHandleIdentifier()).thenReturn(
                "=Testsrcswt.jar_org.eclipse.swt.widgets(Button.class[Button");
    }

    @Test
    public void testWikiServer() {
        final String oldDocument = server.read(element);
        final String write = TESTINPUT.substring(0, (int) (Math.random() * TESTINPUT.length())) + "...";
        server.write(element, write);
        final String document = server.read(element);

        Assert.assertEquals(write, document);
        Assert.assertNotSame(oldDocument, document);

        Server.setDatabase(new CouchDB(null, null));
        final String offlineDoc = server.read(element);
        Assert.assertNotNull(offlineDoc);
    }
}
