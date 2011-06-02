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
        final String oldDocument = server.getText(element);
        final String write = TESTINPUT.substring(0, (int) (Math.random() * TESTINPUT.length())) + "...";
        server.setText(element, write);
        final String document = server.getText(element);

        Assert.assertEquals(write, document);
        Assert.assertNotSame(oldDocument, document);

        Server.setDatabase(new CouchDB(null, null));
        final String offlineDoc = server.getText(element);
        Assert.assertNotNull(offlineDoc);
    }
}
