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
package org.eclipse.recommenders.tests.apidocs;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.utils.Throws;

import com.google.common.collect.Maps;

public class SubscriptionVerifier {

    private Map<JavaElementSelectionEvent, Map<ApidocProvider, String>> subscriptions = Maps.newHashMap();

    public void addResult(JavaElementSelectionEvent selection, ApidocProvider provider, String actualMethodName) {

        ensureIsNotNull(selection);
        ensureIsNotNull(provider);
        ensureIsNotNull(actualMethodName);

        Map<ApidocProvider, String> methodsByProvider = get(selection);

        if (methodsByProvider.put(provider, actualMethodName) != null) {
            Throws.throwIllegalArgumentException("selection was already processed by this provider");
        }
    }

    private Map<ApidocProvider, String> get(JavaElementSelectionEvent selection) {
        Map<ApidocProvider, String> methodsByProvider = subscriptions.get(selection);
        if (methodsByProvider == null) {
            methodsByProvider = Maps.newHashMap();
            subscriptions.put(selection, methodsByProvider);
        }
        return methodsByProvider;
    }

    public void assertSubscription(JavaElementSelectionEvent selection, ApidocProvider provider,
            String expectedMethodName) {
        Map<ApidocProvider, String> methodsByProvider = get(selection);
        String actualMethodName = methodsByProvider.get(provider);
        assertEquals(expectedMethodName, actualMethodName);
        methodsByProvider.remove(provider);
    }

    public void assertNoMoreSubscriptions() {
        for (Map<ApidocProvider, String> methodsByProvider : subscriptions.values()) {
            assertEquals(0, methodsByProvider.size());
        }
    }
}
