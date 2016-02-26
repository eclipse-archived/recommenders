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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeedDescriptorTest {

    private static final String FIRST_ELEMENT = "first";

    @Test(expected = IllegalArgumentException.class)
    public void testFailUrlMalformed() {
        IConfigurationElement config = Mockito.mock(IConfigurationElement.class);
        when(config.getAttribute(ATTRIBUTE_ID)).thenReturn(FIRST_ELEMENT);
        when(config.getAttribute(ATTRIBUTE_URI)).thenReturn("abc");
        FeedDescriptor.fromConfigurationElement(config, true, null);
    }
}
