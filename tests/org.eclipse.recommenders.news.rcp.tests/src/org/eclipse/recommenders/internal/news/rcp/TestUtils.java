/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.eclipse.recommenders.internal.news.rcp.Constants.*;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.mockito.Mockito;

public class TestUtils {

    private static final String TEST_URL = "http://planeteclipse.org/planet/rss20.xml";

    public static FeedDescriptor enabled(String id) {
        IConfigurationElement config = Mockito.mock(IConfigurationElement.class);
        when(config.getAttribute(ATTRIBUTE_ID)).thenReturn(id);
        when(config.getAttribute(ATTRIBUTE_NAME)).thenReturn(id);
        when(config.getAttribute(ATTRIBUTE_URI)).thenReturn(TEST_URL);
        when(config.getAttribute(ATTRIBUTE_ENABLED_BY_DEFAULT)).thenReturn(Boolean.TRUE.toString());
        return FeedDescriptor.fromConfigurationElement(config, null);
    }

    public static FeedDescriptor disabled(String id) {
        IConfigurationElement config = Mockito.mock(IConfigurationElement.class);
        when(config.getAttribute(ATTRIBUTE_ID)).thenReturn(id);
        when(config.getAttribute(ATTRIBUTE_NAME)).thenReturn(id);
        when(config.getAttribute(ATTRIBUTE_URI)).thenReturn(TEST_URL);
        when(config.getAttribute(ATTRIBUTE_ENABLED_BY_DEFAULT)).thenReturn(Boolean.FALSE.toString());
        return FeedDescriptor.fromConfigurationElement(config, null);
    }

    public static FeedDescriptor mockFeed(String name) {
        return enabled(name);
    }
}
