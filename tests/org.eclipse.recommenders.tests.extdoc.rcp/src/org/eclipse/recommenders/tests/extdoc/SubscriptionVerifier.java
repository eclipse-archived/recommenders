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
package org.eclipse.recommenders.tests.extdoc;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Throws;

import com.google.common.collect.Maps;

public class SubscriptionVerifier {

    private Map<JavaSelectionEvent, Map<ExtdocProvider, String>> subscriptions = Maps.newHashMap();

    public void addResult(JavaSelectionEvent selection, ExtdocProvider provider, String actualMethodName) {

        ensureIsNotNull(selection);
        ensureIsNotNull(provider);
        ensureIsNotNull(actualMethodName);

        Map<ExtdocProvider, String> methodsByProvider = get(selection);

        if (methodsByProvider.put(provider, actualMethodName) != null) {
            Throws.throwIllegalArgumentException("selection was already processed by this provider");
        }
    }

    private Map<ExtdocProvider, String> get(JavaSelectionEvent selection) {
        Map<ExtdocProvider, String> methodsByProvider = subscriptions.get(selection);
        if (methodsByProvider == null) {
            methodsByProvider = Maps.newHashMap();
            subscriptions.put(selection, methodsByProvider);
        }
        return methodsByProvider;
    }

    public void assertSubscription(JavaSelectionEvent selection, ExtdocProvider provider, String expectedMethodName) {
        Map<ExtdocProvider, String> methodsByProvider = get(selection);
        String actualMethodName = methodsByProvider.get(provider);
        assertEquals(expectedMethodName, actualMethodName);
        methodsByProvider.remove(provider);
    }

    public void assertNoMoreSubscriptions() {
        for (Map<ExtdocProvider, String> methodsByProvider : subscriptions.values()) {
            assertEquals(0, methodsByProvider.size());
        }
    }
}
