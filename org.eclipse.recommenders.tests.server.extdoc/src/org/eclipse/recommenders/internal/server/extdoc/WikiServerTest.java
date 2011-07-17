/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.server.extdoc.WikiServer;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.junit.Test;
import org.mockito.Mockito;

public final class WikiServerTest {

    private static final String TESTINPUT = "This is a test using Button. I like using Button.";

    private final WikiServer server = new WikiServer(ServerUtils.getServer(), ServerUtils.getUsernameListener(),
            ExtDocUtils.getResolver());
    private final IJavaElement element;

    public WikiServerTest() {
        element = Mockito.mock(IJavaElement.class);
        Mockito.when(element.getHandleIdentifier()).thenReturn(
                "=Testsrcswt.jar_org.eclipse.swt.widgets(Button.class[Button");
    }

    @Test
    public void testWikiServer() throws InterruptedException {
        final String oldDocument = server.getText(element);
        final String write = TESTINPUT.substring(0, (int) (Math.random() * TESTINPUT.length())) + "...";
        server.setText(element, write);
        final String document = server.getText(element);

        // Assert.assertEquals(write, document);
        // Assert.assertNotSame(oldDocument, document);
    }
}
