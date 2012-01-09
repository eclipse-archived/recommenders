/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProviderDescription;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

public class ExtdocProviderTest {
    public ExtdocProvider sut;

    @Before
    public void setup() {
        sut = new ExtdocProvider() {
        };
    }

    @Test
    public void assertTestIsNotRunInUIThread() {
        Thread actual = Thread.currentThread();
        Thread unexpected = Display.getDefault().getThread();
        assertNotSame(unexpected, actual);
    }

    @Test
    public void descriptionCanBeSet() {
        ExtdocProviderDescription expected = mock(ExtdocProviderDescription.class);
        sut.setDescription(expected);
        ExtdocProviderDescription actual = sut.getDescription();
        assertSame(expected, actual);
    }

    @Test
    public void providerDescriptionReturnsSetValues() {
        String name = "a provider name";
        Image image = createExampleImage();

        ExtdocProviderDescription sut = new ExtdocProviderDescription(name, image);
        String actualName = sut.getName();
        Image actualImage = sut.getImage();

        assertEquals(name, actualName);
        assertEquals(image, actualImage);
    }

    private Image createExampleImage() {
        return new Image(Display.getDefault(), new Rectangle(0, 0, 1, 1));
    }

    @Test
    public void statusCanBeSet() {
        sut.setEnabled(true);
        assertTrue(sut.isEnabled());
        sut.setEnabled(false);
        assertFalse(sut.isEnabled());
    }

    @Test
    public void runnablesCanBeQueuedInUiThread() {
        final boolean[] isRun = { false };

        sut = new ExtdocProvider() {
            {
                runSyncInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRun[0] = true;
                    }
                });
            }
        };

        assertTrue(isRun[0]);
    }

    @Test
    public void exceptionsFromFailingRunnablesAreBubbledOutOfUiThread() {
        final boolean[] isCatched = { false };

        sut = new ExtdocProvider() {
            {
                try {
                    runSyncInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            throw new RuntimeException();
                        }
                    });
                } catch (RuntimeException e) {
                    isCatched[0] = true;
                }
            }
        };

        assertTrue(isCatched[0]);
    }
}